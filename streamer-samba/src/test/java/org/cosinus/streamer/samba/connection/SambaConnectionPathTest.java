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

package org.cosinus.streamer.samba.connection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SambaConnectionPathTest {

    @Test
    public void testToSmbPath() {
        try (SambaConnection connection = new SambaConnection("test", null, null, null)) {
            assertEquals("", connection.toSmbPath(""));
            assertEquals("a\\b\\file.txt", connection.toSmbPath("a/b/file.txt"));
            assertEquals("a\\b", connection.toSmbPath("/a/b/"));
        }
    }

    @Test
    public void testToSmbPathWithRemoteDir() {
        try (SambaConnection connection = new SambaConnection("test", null, null, "/base/dir/")) {
            assertEquals("base\\dir", connection.toSmbPath(""));
            assertEquals("base\\dir\\a\\file.txt", connection.toSmbPath("a/file.txt"));
        }
    }

    @Test
    public void testSambaFileName() {
        assertEquals("file.txt", SambaFile.sambaFile("file.txt").name());
        assertEquals("file.txt", SambaFile.sambaFile("a/b/file.txt").name());
        assertEquals("", SambaFile.sambaFolder("").name());
    }
}
