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

package org.cosinus.streamer.api.transform.xml;

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class XmlToJsonBinaryStreamerTransformerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private final XmlToJsonBinaryStreamerTransformer transformer = new XmlToJsonBinaryStreamerTransformer();

    @Test
    public void testCanTransformXmlToJson() {
        assertTrue(transformer.canTransform(file("data.xml"), "json"));
        assertTrue(transformer.canTransform(file("DATA.XML"), "JSON"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("data.xml"), "csv"));
        assertFalse(transformer.canTransform(file("data.xml"), "xml"));
        assertFalse(transformer.canTransform(file("data.xml"), null));
        assertFalse(transformer.canTransform(file("data.json"), "json"));
        assertFalse(transformer.canTransform(file("data"), "json"));
        assertFalse(transformer.canTransform(image("data.xml", new byte[0]), "json"));
    }

    @Test
    public void testTransformXml() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!-- a comment -->
            <library city="Iași">
              <book id="1" lang="ro">
                <title>Ion</title>
                <author>Liviu Rebreanu</author>
                <year>1920</year>
              </book>
              <book id="2">
                <title>Moara cu noroc</title>
                <author>Ioan Slavici</author>
                <code>007</code>
              </book>
              <empty/>
              <note type="info">Ștefan &amp; &lt;Ana&gt;</note>
              <mixed>before <b>bold</b> after</mixed>
              <?processing instruction?>
              <data><![CDATA[<raw> & text]]></data>
            </library>
            """;
        assertEquals("""
            {
              "library" : {
                "city" : "Iași",
                "book" : [ {
                  "id" : "1",
                  "lang" : "ro",
                  "title" : "Ion",
                  "author" : "Liviu Rebreanu",
                  "year" : "1920"
                }, {
                  "id" : "2",
                  "title" : "Moara cu noroc",
                  "author" : "Ioan Slavici",
                  "code" : "007"
                } ],
                "empty" : "",
                "note" : {
                  "type" : "info",
                  "#text" : "Ștefan & <Ana>"
                },
                "mixed" : {
                  "b" : "bold",
                  "#text" : "before  after"
                },
                "data" : "<raw> & text"
              }
            }""", transform(xml.getBytes(UTF_8)));
    }

    @Test
    public void testNamespacePrefixesAreKept() {
        String xml = "<a:root xmlns:a=\"urn:a\"><a:item>x</a:item></a:root>";
        assertEquals("""
            {
              "a:root" : {
                "xmlns:a" : "urn:a",
                "a:item" : "x"
              }
            }""", transform(xml.getBytes(UTF_8)));
    }

    /**
     * An attribute and a child element with the same name are put in an array, the attribute first.
     */
    @Test
    public void testAttributeAndChildElementWithTheSameName() {
        String xml = "<book title=\"short\"><title>long title</title><title>other</title></book>";
        assertEquals("""
            {
              "book" : {
                "title" : [ "short", "long title", "other" ]
              }
            }""", transform(xml.getBytes(UTF_8)));
    }

    @Test
    public void testEncodingOfTheXmlDeclaration() {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-2\"?><oraş>Braşov, Ţara Bârsei, Făgăraş</oraş>";
        assertEquals("{\n  \"oraş\" : \"Braşov, Ţara Bârsei, Făgăraş\"\n}",
            transform(xml.getBytes(Charset.forName("ISO-8859-2"))));
    }

    /**
     * An external entity is not loaded, so the xml cannot read the content of other files.
     */
    @Test
    public void testExternalEntitiesAreNotLoaded() throws IOException {
        File secret = folder.newFile("secret.txt");
        Files.writeString(secret.toPath(), "top secret");
        String xml = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE root [<!ENTITY secret SYSTEM \"" + secret.toURI() + "\">]>"
            + "<root>&secret;</root>";

        String json = transform(xml.getBytes(UTF_8));
        assertFalse(json, json.contains("top secret"));
    }

    @Test
    public void testInternalEntitiesAreExpanded() {
        String xml = "<!DOCTYPE root [<!ENTITY name \"Ștefan\">]><root>Hello &name;</root>";
        assertEquals("{\n  \"root\" : \"Hello Ștefan\"\n}", transform(xml.getBytes(UTF_8)));
    }

    /**
     * The entity expansions are limited, so a small content cannot expand to a huge text ("billion laughs").
     */
    @Test
    public void testEntityExpansionIsLimited() {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\"?><!DOCTYPE root [<!ENTITY lol0 \"lol\">");
        for (int level = 1; level < 10; level++) {
            xml.append("<!ENTITY lol").append(level).append(" \"")
                .append(("&lol" + (level - 1) + ";").repeat(10)).append("\">");
        }
        xml.append("]><root>&lol9;</root>");
        TestBinaryStreamer source = new TestBinaryStreamer("data.xml", xml.toString().getBytes(UTF_8));
        assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "json")));
    }

    @Test
    public void testInvalidContentFails() {
        for (byte[] content : List.of("<root><unclosed></root>".getBytes(UTF_8), "not xml".getBytes(UTF_8), new byte[0])) {
            TestBinaryStreamer source = new TestBinaryStreamer("data.xml", content);
            assertThrows(UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "json")));
        }
    }

    private String transform(byte[] xml) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer("data.xml", xml), "json")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
