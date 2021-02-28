/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.file;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.DirectoryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.consumer.StreamConsumer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_COMPUTER;

@RootStreamer("Filesystem")
@ConditionalOnProperty(name = "streamer.file.enabled", matchIfMissing = true)
public class FileMainStreamer extends MainStreamer<FileStreamer> {

    public static final String FILE_PROTOCOL = "file://";

    private final FileHandler fileHandler;

    private final ProcessExecutor processExecutor;

    private List<FileStreamer> roots;

    public FileMainStreamer(FileHandler fileHandler,
                            ProcessExecutor processExecutor) {
        this.fileHandler = fileHandler;
        this.processExecutor = processExecutor;
    }

    @Override
    public Stream<FileStreamer> stream() {
        return getRoots().stream();
    }

    @Override
    public StreamConsumer<FileStreamer> saver(boolean append) {
        return null;
    }

    @Override
    public DirectoryStreamer createDirectoryStreamer(Path path) {
        return null;
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path) {
        return null;
    }

    @Override
    public Stream<FileStreamer> flatStream(StreamerFilter streamerFilter) {
        return stream()
                .filter(streamerFilter)
                .map(Streamer::getPath)
                .flatMap(fileHandler::walk)
                .map(this::create);
    }

    public Optional<FileStreamer> findRoot(Path path) {
        return getRoots()
                .stream()
                .filter(root -> fileHandler.isSameFile(path, root.getPath()))
                .findFirst();
    }

    protected List<FileStreamer> getRoots() {
        if (roots == null) {
            updateRoots();
        }
        return roots;
    }

    protected void updateRoots() {
        roots = fileHandler.roots()
                .map(this::createFileRootElement)
                .collect(Collectors.toList());
    }

    protected FileRootStreamer createFileRootElement(Path path) {
        return new FileRootStreamer(this, fileHandler, path);
    }


    public Optional<Streamer> find(Path path) {
        return findRoot(path)
                .map(Streamer.class::cast)
                .or(() -> Optional.of(path)
                        .map(Path::toFile)
                        .filter(File::exists)
                        .map(file -> create(path, file.isDirectory())));
    }

    @Override
    public FileStreamer create(Path path) {
        File file = path.toFile();
        boolean directory = !file.exists() || file.isDirectory();
        return create(path, directory);
    }

    //@Override
    public FileStreamer create(Path path, boolean directory) {
        return directory ?
                new FileDirectoryStreamer(this, fileHandler, path) :
                new FileBinaryStreamer(this, fileHandler, path);
//                fileHandler.mimeType(path).startsWith("text") ?
//                        new TextFileStreamer(this, fileHandler, path) :
//                        new BinaryFileStreamer(this, fileHandler, path);
    }

    @Override
    public boolean exists() {
        return false;
    }


    @Override
    public String getIconName() {
        return ICON_COMPUTER;
    }

    @Override
    public boolean isPack() {
        return false;
    }

    @Override
    public boolean canRead() {
        return false;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean isSensitiveToTransferType() {
        return false;
    }

    @Override
    public boolean isCompatible(String urlPath) {
        return urlPath.startsWith(FILE_PROTOCOL);
    }

    @Override
    public Optional<Streamer> findByUrlPath(String urlPath) {
        return Optional.ofNullable(urlPath)
                .filter(path -> path.startsWith(FILE_PROTOCOL))
                .map(path -> path.substring(FILE_PROTOCOL.length()))
                .map(Paths::get)
                .flatMap(this::find);
    }

    @Override
    public void execute(Path path) {
        processExecutor.executeFile(path.toFile());
    }

    @Override
    public boolean rename(Path path, String newName) {
        File fileToRename = path.toFile();

        Path newPath = Paths.get(fileToRename.getParent(), newName);
        File newFile = newPath.toFile();
        boolean samePath = fileToRename.toPath().equals(newPath);
        if (newFile.exists() && !samePath) {
            throw new StreamerException("rename.error.already.exist", newPath);
        }

        if (!samePath) {
            return fileToRename.renameTo(newFile);
        }

        try (AutoRemovableTemporaryFile tmpFile = new AutoRemovableTemporaryFile(fileToRename)) {
            return fileToRename.renameTo(tmpFile.getFile()) &&
                    tmpFile.getFile().renameTo(newFile);
        } catch (IOException ex) {
            throw new StreamerException("rename.error.cannot.create.temporary.file", fileToRename, ex);
        }
    }
}
