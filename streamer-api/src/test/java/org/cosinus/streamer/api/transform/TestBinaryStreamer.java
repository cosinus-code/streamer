/*
 * Copyright 2026 Cosinus Software
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

package org.cosinus.streamer.api.transform;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * In-memory {@link BinaryStreamer} for tests, with the type given by the extension of its name.
 */
public class TestBinaryStreamer implements BinaryStreamer {

    private final Path path;

    private final byte[] content;

    private final boolean image;

    public TestBinaryStreamer(String name, byte[] content) {
        this(name, content, false);
    }

    public TestBinaryStreamer(String name, byte[] content, boolean image) {
        this.path = Paths.get(name);
        this.content = content;
        this.image = image;
    }

    public static TestBinaryStreamer image(String name, byte[] content) {
        return new TestBinaryStreamer(name, content, true);
    }

    /**
     * @param binaryStreamer the streamer to read
     * @return all the bytes provided by the input stream of the given streamer
     */
    public static byte[] readAll(BinaryStreamer binaryStreamer) {
        try (InputStream input = binaryStreamer.inputStream()) {
            return input.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return null;
    }

    @Override
    public boolean isImage() {
        return image;
    }

    @Override
    public InputStream inputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return new ByteArrayOutputStream();
    }
}
