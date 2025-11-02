/*
 * Copyright 2025 Cosinus Software
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
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.file.FileSystemRoot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.cosinus.swing.image.icon.IconProvider.ICON_COMPUTER;

@RootStreamer("Filesystem")
@ConditionalOnProperty(name = "streamer.file.enabled", matchIfMissing = true)
public class FileMainStreamer extends MainStreamer<FileStreamer<?>> {

    public static final String FILE_PROTOCOL = "file://";

    private final FileHandler fileHandler;

    private List<TranslatableName> detailNames;

    public FileMainStreamer(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @Override
    public Stream<FileStreamer<?>> stream() {
        return getRoots().stream();
    }

    @Override
    public Stream<FileStreamer<?>> flatStream(StreamerFilter streamerFilter) {
        return Stream.empty();
    }

    @Override
    public String getProtocol() {
        return FILE_PROTOCOL;
    }

    public Optional<FileStreamer<?>> findRoot(Path path) {
        return getRoots()
            .stream()
            .filter(root -> fileHandler.isSameFile(path, root.getPath()))
            .findFirst();
    }

    protected List<FileStreamer<?>> getRoots() {
        return fileHandler.getFileSystemRoots()
            .stream()
            .map(this::createFileRootStreamer)
            .collect(Collectors.toList());
    }

    protected FileRootStreamer createFileRootStreamer(FileSystemRoot fileSystemRoot) {
        return new FileRootStreamer(fileSystemRoot);
    }

    public Optional<Streamer<?>> findByPath(Path path) {
        if (Objects.equals(path, getPath())) {
            return Optional.of(this);
        }

        return findRoot(path)
            .or(() -> Optional.of(path)
                .map(Path::toFile)
                .map(file -> create(path, file.isDirectory())))
            .map(Streamer.class::cast);
    }

    public FileStreamer<?> create(Path path) {
        File file = path.toFile();
        boolean directory = !file.exists() || file.isDirectory();
        return create(path, directory);
    }

    @Override
    public FileStreamer<?> create(Path path, boolean directory) {
        return directory ?
            new FileParentStreamer(path) :
            new FileBinaryStreamer(path);
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
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void execute(Path path) {
        fileHandler.open(path.toFile());
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public void init() {
        detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_TYPE, null),
            new TranslatableName(DETAIL_KEY_FREE_MEMORY, null),
            new TranslatableName(DETAIL_KEY_TOTAL_MEMORY, null)
        );
    }

    @Override
    public void reset() {
        fileHandler.reset();
    }
}
