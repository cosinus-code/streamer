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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.swing.exec.ProcessExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import oshi.software.os.OSFileStore;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_COMPUTER;

@RootStreamer("Filesystem")
@ConditionalOnProperty(name = "streamer.file.enabled", matchIfMissing = true)
public class FileMainStreamer extends MainStreamer<FileStreamer<?>> {

    public static final String FILE_PROTOCOL = "file://";

    private final FileHandler fileHandler;

    private final ProcessExecutor processExecutor;

    public FileMainStreamer(FileHandler fileHandler,
                            ProcessExecutor processExecutor) {
        this.fileHandler = fileHandler;
        this.processExecutor = processExecutor;
    }

    @Override
    public Stream<FileRootStreamer> stream() {
        return getRoots().stream();
    }

    @Override
    public Stream<FileStreamer<?>> flatStream(StreamerFilter streamerFilter) {
        return stream()
            .filter(streamerFilter)
            .map(Streamer::getPath)
            .flatMap(fileHandler::walk)
            .map(this::create);
    }

    public Optional<FileRootStreamer> findRoot(Path path) {
        return getRoots()
            .stream()
            .filter(root -> fileHandler.isSameFile(path, root.getPath()))
            .findFirst();
    }

    protected List<FileRootStreamer> getRoots() {
        return fileHandler.getFileStores()
            .stream()
            .map(this::createFileRootStreamer)
            .collect(Collectors.toList());
    }

    protected FileRootStreamer createFileRootStreamer(OSFileStore fileStore) {
        return new FileRootStreamer(this, fileHandler, fileStore);
    }

    public Optional<FileStreamer> find(Path path) {
        return findRoot(path)
            .map(FileStreamer.class::cast)
            .or(() -> Optional.of(path)
                .map(Path::toFile)
                .map(file -> create(path, file.isDirectory())));
    }

    public FileStreamer<?> create(Path path) {
        File file = path.toFile();
        boolean directory = !file.exists() || file.isDirectory();
        return create(path, directory);
    }

    @Override
    public FileStreamer<?> create(Path path, boolean directory) {
        return directory ?
            new FileParentStreamer(this, fileHandler, path) :
            new FileBinaryStreamer(this, fileHandler, path);
    }

    @Override
    public boolean exists() {
        return true;
    }


    @Override
    public String getIconName() {
        return ICON_COMPUTER;
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
}
