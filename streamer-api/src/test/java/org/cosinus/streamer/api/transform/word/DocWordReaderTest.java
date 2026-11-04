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

package org.cosinus.streamer.api.transform.word;

import org.cosinus.streamer.api.transform.word.WordElement.WordParagraph;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocWordReaderTest {

    @Test
    public void testTextIsCleaned() {
        assertEquals("line one\nline two", DocWordReader.clean("line one\u000Bline two\r"));
        assertEquals("cell", DocWordReader.clean("cell\u0007"));
        assertEquals("Page 3 of the text", DocWordReader.clean("Page \u0013 PAGE \u00143\u0015 of the text\u0001\r"));
        assertEquals("before\fafter", DocWordReader.clean("before\fafter\r"));
    }

    @Test
    public void testFontSizesAreInPoints() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/org/cosinus/streamer/api/transform/pdf/sample.doc")) {
            assertNotNull(input);
            List<WordElement> elements = DocWordReader.read(input);
            WordParagraph heading = (WordParagraph) elements.getFirst();
            assertEquals("Introducere", heading.text());
            assertEquals("heading1", heading.style());
            assertEquals(24, heading.fontSize(), 0.01);
            WordParagraph body = (WordParagraph) elements.get(1);
            assertEquals(12, body.fontSize(), 0.01);
        }
    }
}
