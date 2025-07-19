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

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.error.SaveStreamerException;

import java.io.*;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;

public class FileBinaryStreamer extends FileStreamer<byte[]> implements BinaryStreamer {

    public FileBinaryStreamer(Path path) {
        super(path);
    }

    @Override
    public InputStream inputStream() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public OutputStream outputStream(boolean append) {
        try {
            ofNullable(file.getParentFile())
                .ifPresent(File::mkdirs);
            return new FileOutputStream(file, append);
        } catch (FileNotFoundException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void createAndSave() {
        try {
            if (!file.createNewFile()) {
                throw new SaveStreamerException("Failed to create file:" + file.getPath());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public boolean isTextCompatible() {
        return fileHandler.isTextCompatible(getPath());
    }

    @Override
    public boolean isImage() {
        return fileHandler.isImage(getPath());
    }

    @Override
    public void finalizeCopy(final BinaryStreamer source) {
        File sourceFile = source.getPath().toFile();
        if (sourceFile.exists()) {
            fileHandler.copyPermissions(sourceFile, file);
        }
    }
}
