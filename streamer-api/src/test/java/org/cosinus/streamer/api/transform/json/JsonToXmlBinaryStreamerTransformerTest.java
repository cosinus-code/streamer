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

package org.cosinus.streamer.api.transform.json;

import org.cosinus.streamer.api.transform.TestBinaryStreamer;
import org.cosinus.streamer.api.transform.xml.XmlToJsonBinaryStreamerTransformer;
import org.junit.Test;
import tools.jackson.databind.json.JsonMapper;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_16LE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.image;
import static org.cosinus.streamer.api.transform.TestBinaryStreamer.readAll;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class JsonToXmlBinaryStreamerTransformerTest {

    private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private final JsonToXmlBinaryStreamerTransformer transformer = new JsonToXmlBinaryStreamerTransformer();

    @Test
    public void testCanTransformJsonToXml() {
        assertTrue(transformer.canTransform(file("data.json"), "xml"));
        assertTrue(transformer.canTransform(file("DATA.JSON"), "XML"));
    }

    @Test
    public void testCannotTransformOtherSourceOrTarget() {
        assertFalse(transformer.canTransform(file("data.json"), "csv"));
        assertFalse(transformer.canTransform(file("data.json"), "json"));
        assertFalse(transformer.canTransform(file("data.json"), null));
        assertFalse(transformer.canTransform(file("data.xml"), "xml"));
        assertFalse(transformer.canTransform(file("data"), "xml"));
        assertFalse(transformer.canTransform(image("data.json", new byte[0]), "xml"));
    }

    @Test
    public void testTransformJson() {
        String json = """
            {
              "library": {
                "city": "Iași",
                "book": [
                  {"id": 1, "title": "Ion", "available": true},
                  {"id": 2, "title": "Moara cu noroc", "code": "007", "price": 12.5}
                ],
                "empty": null,
                "nothing": {},
                "note": {"type": "info", "#text": "Ștefan & <Ana>"},
                "matrix": [[1, 2], [3]]
              }
            }
            """;
        assertEquals(DECLARATION + """
            <library city="Iași">
              <book id="1" title="Ion" available="true"/>
              <book id="2" title="Moara cu noroc" code="007" price="12.5"/>
              <empty/>
              <nothing/>
              <note type="info">Ștefan &amp; &lt;Ana></note>
              <matrix>
                <item>1</item>
                <item>2</item>
              </matrix>
              <matrix>
                <item>3</item>
              </matrix>
            </library>
            """, transform(json.getBytes(UTF_8)));
    }

    /**
     * Anything else than an object with a single member which is not an array is written in a root element.
     */
    @Test
    public void testRootElement() {
        assertEquals(DECLARATION + "<root a=\"1\" b=\"2\"/>\n", transform("{\"a\": 1, \"b\": 2}"));
        assertEquals(DECLARATION + "<root>\n  <a>1</a>\n  <a>2</a>\n</root>\n", transform("{\"a\": [1, 2]}"));
        assertEquals(DECLARATION + "<root>\n  <item>x</item>\n  <item/>\n</root>\n", transform("[\"x\", null]"));
        assertEquals(DECLARATION + "<root>text</root>\n", transform("\"text\""));
        assertEquals(DECLARATION + "<root/>\n", transform("{}"));
    }

    @Test
    public void testNamesAreMadeValid() {
        assertEquals("first_name", JsonToXmlBinaryStreamer.xmlName("first name"));
        assertEquals("_1st", JsonToXmlBinaryStreamer.xmlName("1st"));
        assertEquals("_-x", JsonToXmlBinaryStreamer.xmlName("-x"));
        assertEquals("_", JsonToXmlBinaryStreamer.xmlName(""));
        assertEquals("a_b_", JsonToXmlBinaryStreamer.xmlName("a<b>"));
        assertEquals("oraș.vechi-1", JsonToXmlBinaryStreamer.xmlName("oraș.vechi-1"));
        assertEquals("a:item", JsonToXmlBinaryStreamer.xmlName("a:item"));
        assertEquals("_:x", JsonToXmlBinaryStreamer.xmlName(":x"));
    }

    @Test
    public void testInvalidXmlCharactersAreRemoved() {
        assertEquals(DECLARATION + "<a>bell and tab\t</a>\n", transform("{\"a\": \"bell\\u0007 and tab\\t\"}"));
    }

    /**
     * The xml converted to json is converted back to the same xml
     * (with the attributes in alphabetical order, as given by the xml parser).
     */
    @Test
    public void testRoundTripFromXml() {
        String xml = "<a:library xmlns:a=\"urn:a\" city=\"Iași\"><a:book>Ion</a:book><a:book>Baltagul</a:book></a:library>";
        byte[] json = readAll(new XmlToJsonBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("data.xml", xml.getBytes(UTF_8)), "json"));
        assertEquals(DECLARATION + """
            <a:library city="Iași" xmlns:a="urn:a">
              <a:book>Ion</a:book>
              <a:book>Baltagul</a:book>
            </a:library>
            """, transform(json));
    }

    /**
     * The special characters are escaped, so the xml is read back with the same text.
     */
    @Test
    public void testSpecialCharactersAreReadBack() {
        String text = "a ]]> b & <c> 'single'";
        byte[] xml = transform("{\"t\": \"" + text + "\"}").getBytes(UTF_8);
        byte[] json = readAll(new XmlToJsonBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("data.xml", xml), "json"));
        assertEquals("{\n  \"t\" : \"" + text + "\"\n}", new String(json, UTF_8));
    }

    /**
     * The special characters of an attribute are escaped, so the xml is read back with the same value.
     */
    @Test
    public void testSpecialCharactersOfAttributesAreReadBack() {
        String text = "a & <b> \"double\" 'single'";
        String json = JsonMapper.builder().build().writeValueAsString(Map.of("r", Map.of("t", text)));
        String xml = transform(json);
        assertTrue(xml, xml.contains("<r t=\""));

        byte[] back = readAll(new XmlToJsonBinaryStreamerTransformer()
            .transformStreamer(new TestBinaryStreamer("data.xml", xml.getBytes(UTF_8)), "json"));
        assertEquals(text, JsonMapper.builder().build().readTree(back).get("r").get("t").asString());
    }

    /**
     * A text with a line break, a value in an array and a name which is already an attribute stay elements:
     * an attribute would lose the line break, and cannot be repeated.
     */
    @Test
    public void testValuesWhichCannotBeAttributes() {
        String json = """
            {"r": {"line": "first\\nsecond", "tag": ["a", "b"], "a b": 1, "a_b": 2, "#text": "text"}}""";
        assertEquals(DECLARATION + """
            <r a_b="1">text
              <line>first
            second</line>
              <tag>a</tag>
              <tag>b</tag>
              <a_b>2</a_b>
            </r>
            """, transform(json));
    }

    @Test
    public void testUtf16Json() {
        assertEquals(DECLARATION + "<oraș>Brașov</oraș>\n", transform("{\"oraș\": \"Brașov\"}".getBytes(UTF_16LE)));
    }

    @Test
    public void testInvalidContentFails() {
        for (String content : List.of("{\"a\": ", "not json", "{} {}", "")) {
            TestBinaryStreamer source = new TestBinaryStreamer("data.json", content.getBytes(UTF_8));
            assertThrows(content, UncheckedIOException.class, () -> readAll(transformer.transformStreamer(source, "xml")));
        }
    }

    private String transform(String json) {
        return transform(json.getBytes(UTF_8));
    }

    private String transform(byte[] json) {
        return new String(readAll(transformer.transformStreamer(new TestBinaryStreamer("data.json", json), "xml")), UTF_8);
    }

    private TestBinaryStreamer file(String name) {
        return new TestBinaryStreamer(name, new byte[0]);
    }
}
