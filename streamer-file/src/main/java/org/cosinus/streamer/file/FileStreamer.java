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

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.file.BaseFileStreamer;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.swing.file.Permissions;
import org.cosinus.swing.util.AutoRemovableTemporaryFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.file.FileMainStreamer.FILE_PROTOCOL;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class FileStreamer<T> extends BaseFileStreamer<T> {

    @Autowired
    protected FileMainStreamer fileMainStreamer;

    @Getter
    protected File file;

    public FileStreamer(Path path) {
        injectContext(this);
        this.file = ofNullable(path)
            .map(Path::toFile)
            .orElse(null);
    }

    @Override
    public Path getPath() {
        return ofNullable(file)
            .map(File::toPath)
            .orElse(null);
    }

    @Override
    public ParentStreamer<?> getParent() {
        return ofNullable(getPath().getParent())
            .map(parentPath -> fileMainStreamer
                .stream()
                .filter(fileStreamer -> parentPath.equals(fileStreamer.getPath()))
                .findFirst()
                .map(ParentStreamer.class::cast)
                .orElseGet(() -> new FileParentStreamer(parentPath)))
            .orElse(fileMainStreamer);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public void save() {
        if (exists()) {
            ofNullable(details.get(DETAIL_INDEX_NAME))
                .map(Value::toString)
                .filter(not(getName()::equals))
                .ifPresent(this::rename);
        } else {
            createAndSave();
        }
    }

    protected abstract void createAndSave();

    protected boolean rename(String newName) {
        File fileToRename = file;
        Path newPath = Paths.get(fileToRename.getParent(), newName);
        boolean samePath = fileToRename.toPath().equals(newPath);
        if (!samePath) {
            return rename(newPath);
        }

        try (AutoRemovableTemporaryFile tmpFile = new AutoRemovableTemporaryFile(fileToRename)) {
            return fileToRename.renameTo(tmpFile.getFile()) &&
                tmpFile.getFile().renameTo(newPath.toFile());
        } catch (IOException ex) {
            throw new StreamerException("rename.error.cannot.create.temporary.file", fileToRename, ex);
        }
    }

    @Override
    public boolean rename(Path newPath) {
        File newFile = newPath.toFile();
        if (newFile.exists()) {
            throw new StreamerException("rename.error.already.exist", newPath);
        }
        return file.renameTo(newPath.toFile());
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canUpdate() {
        return file.canWrite();
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return fileHandler.delete(file, moveToTrash) && !file.exists();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public String getProtocol() {
        return FILE_PROTOCOL;
    }

    @Override
    public boolean isLink() {
        return fileHandler.isLink(file);
    }

    @Override
    public Streamer<?> getLinkedStreamer() {
        return ofNullable(fileHandler.getLinkedPath(file))
            .map(fileMainStreamer::create)
            .orElse(null);
    }

    @Override
    public Permissions getPermissions() {
        return fileHandler.getFilePermissions(file);
    }

    @Override
    public void setPermissions(Permissions permissions) {
        ofNullable(getPermissions())
            .filter(Permissions::isEditable)
            .ifPresent(existingPermissions -> {
                if (!permissions.getNumberView().equals(existingPermissions.getNumberView())) {
                    fileHandler.setFilePermissions(file, permissions);
                }
                String newOwnerName = !permissions.getOwnerName().equals(existingPermissions.getOwnerName()) ?
                    permissions.getOwnerName() :
                    null;
                String newGroupName = !permissions.getGroupName().equals(existingPermissions.getGroupName()) ?
                    permissions.getGroupName() :
                    null;
                if (newOwnerName != null || newGroupName != null) {
                    fileHandler.setOwnerForFile(file, newOwnerName, newGroupName);
                }
            });
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FileStreamer that)) {
            return false;
        }

        return file.equals(that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
