/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.api.transform.json;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS;

/**
 * {@link TransformBinaryStreamer} that converts json content into xml content.
 * <p>
 * The json is written as elements, the reverse of {@link org.cosinus.streamer.api.transform.xml.XmlToJsonBinaryStreamer}:
 * <ul>
 *     <li>an object with a single member which is not an array is written as the root element
 *     (with its text, if it is a string, number or boolean),
 *     anything else is written in a {@value #ROOT} root element;</li>
 *     <li>a string, number or boolean member of an object is written as an attribute of its element,
 *     unless it has a line break (which an attribute would not keep), or its name is already an attribute;</li>
 *     <li>the other members are written as child elements named as the member,
 *     and a "{@value #TEXT}" member as the text of the element;</li>
 *     <li>an array member is written as an element named as the member for each item
 *     (the strings, numbers and booleans as the text of the elements, as an attribute cannot be repeated),
 *     and the items of an array nested in an array as {@value #ITEM} elements;</li>
 *     <li>null, an empty object and an empty array are written as an empty element.</li>
 * </ul>
 * So the "xmlns" and "xmlns:*" members are written as namespace declarations,
 * and the names with a namespace prefix stay valid.
 * A name which is not a valid xml name is made valid: each invalid character is replaced by "_",
 * and a name not starting with a letter or "_" is prefixed by "_".
 * The characters not allowed in xml (e.g. most control characters) are removed.
 * <p>
 * The encoding of the json (utf-8, utf-16 or utf-32) is detected by the parser.
 * The xml is written in utf-8, indented.
 */
public class JsonToXmlBinaryStreamer extends TransformBinaryStreamer<JsonNode> {

    static final String ROOT = "root";

    static final String ITEM = "item";

    static final String TEXT = "#text";

    private static final String DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private static final String INDENT = "  ";

    private final JsonMapper jsonMapper;

    public JsonToXmlBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        this.jsonMapper = JsonMapper
            .builder()
            .enable(FAIL_ON_TRAILING_TOKENS)
            .build();
    }

    @Override
    protected JsonNode read(final InputStream input) throws IOException {
        try {
            JsonNode json = jsonMapper.readTree(input);
            if (json == null || json.isMissingNode()) {
                throw new IOException("Empty json content");
            }
            return json;
        } catch (JacksonException e) {
            throw new IOException("Invalid json content: " + e.getOriginalMessage(), e);
        }
    }

    @Override
    protected void write(final JsonNode json, final OutputStream output) throws IOException {
        try {
            output.write(DECLARATION.getBytes(UTF_8));
            XMLStreamWriter xml = XMLOutputFactory.newInstance().createXMLStreamWriter(output, UTF_8.name());
            Map.Entry<String, JsonNode> root = rootMember(json);
            if (root != null) {
                writeElement(xml, root.getKey(), root.getValue(), 0);
            } else {
                writeElement(xml, ROOT, json, 0);
            }
            xml.writeCharacters("\n");
            xml.flush();
            xml.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return the single member of an object, if it is not an array, to be written as the root element, or null
     */
    private Map.Entry<String, JsonNode> rootMember(final JsonNode json) {
        if (json.isObject() && json.size() == 1) {
            Map.Entry<String, JsonNode> member = json.properties().iterator().next();
            if (!member.getValue().isArray() && !TEXT.equals(member.getKey()) && !isNamespace(member.getKey())) {
                return member;
            }
        }
        return null;
    }

    private void writeElement(final XMLStreamWriter xml, final String name, final JsonNode value, final int depth)
        throws XMLStreamException {
        String elementName = xmlName(name);
        if (value.isNull() || value.isContainer() && value.isEmpty()) {
            xml.writeEmptyElement(elementName);
            return;
        }
        if (value.isValueNode()) {
            xml.writeStartElement(elementName);
            xml.writeCharacters(xmlText(text(value)));
            xml.writeEndElement();
            return;
        }
        if (value.isArray()) {
            // an array, as the content of an element: its items as item elements
            xml.writeStartElement(elementName);
            for (JsonNode item : value) {
                writeMember(xml, ITEM, item, depth + 1);
            }
            newLine(xml, depth);
            xml.writeEndElement();
            return;
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        List<Map.Entry<String, JsonNode>> children = new ArrayList<>();
        JsonNode textNode = null;
        for (Map.Entry<String, JsonNode> member : value.properties()) {
            if (TEXT.equals(member.getKey()) && member.getValue().isValueNode()) {
                textNode = member.getValue();
            } else if (isAttribute(member.getValue()) && !attributes.containsKey(xmlName(member.getKey()))) {
                attributes.put(xmlName(member.getKey()), xmlText(text(member.getValue())));
            } else {
                children.add(member);
            }
        }

        if (children.isEmpty() && textNode == null) {
            xml.writeEmptyElement(elementName);
        } else {
            xml.writeStartElement(elementName);
        }
        for (Map.Entry<String, String> attribute : attributes.entrySet()) {
            xml.writeAttribute(attribute.getKey(), attribute.getValue());
        }
        if (children.isEmpty() && textNode == null) {
            return;
        }
        if (textNode != null) {
            xml.writeCharacters(xmlText(text(textNode)));
        }
        for (Map.Entry<String, JsonNode> child : children) {
            writeMember(xml, child.getKey(), child.getValue(), depth + 1);
        }
        if (!children.isEmpty()) {
            newLine(xml, depth);
        }
        xml.writeEndElement();
    }

    /**
     * @return true for a string, number or boolean, written as an attribute,
     * unless it has a line break, which the xml parsers would change into a space in an attribute
     */
    private static boolean isAttribute(final JsonNode value) {
        return value.isValueNode() && !value.isNull() && value.asString().chars().noneMatch(c -> c == '\n' || c == '\r');
    }

    /**
     * An array member is written as an element for each of its items, the other members as one element.
     */
    private void writeMember(final XMLStreamWriter xml, final String name, final JsonNode value, final int depth)
        throws XMLStreamException {
        if (value.isArray()) {
            for (JsonNode item : value) {
                newLine(xml, depth);
                // an array nested in the array is written as an element with item elements
                writeElement(xml, name, item, depth);
            }
        } else {
            newLine(xml, depth);
            writeElement(xml, name, value, depth);
        }
    }

    private void newLine(final XMLStreamWriter xml, final int depth) throws XMLStreamException {
        xml.writeCharacters("\n" + INDENT.repeat(depth));
    }

    private static String text(final JsonNode value) {
        return value.isNull() ? "" : value.asString();
    }

    private static boolean isNamespace(final String name) {
        return name.equals("xmlns") || name.startsWith("xmlns:");
    }

    /**
     * @return the name, made a valid xml name: each invalid character replaced by "_",
     * and prefixed by "_" if it doesn't start with a letter or "_"
     */
    static String xmlName(final String name) {
        StringBuilder xmlName = new StringBuilder(name.length() + 1);
        name.codePoints().forEach(codePoint -> xmlName.appendCodePoint(isNameChar(codePoint) ? codePoint : '_'));
        if (xmlName.isEmpty() || !isNameStartChar(xmlName.codePointAt(0)) || xmlName.charAt(0) == ':') {
            xmlName.insert(0, '_');
        }
        return xmlName.toString();
    }

    private static boolean isNameStartChar(final int codePoint) {
        return Character.isLetter(codePoint) || codePoint == '_' || codePoint == ':';
    }

    private static boolean isNameChar(final int codePoint) {
        return isNameStartChar(codePoint) || Character.isDigit(codePoint) ||
            codePoint == '-' || codePoint == '.' || codePoint == 0xB7 ||
            Character.getType(codePoint) == Character.NON_SPACING_MARK ||
            Character.getType(codePoint) == Character.COMBINING_SPACING_MARK;
    }

    /**
     * @return the text, without the characters not allowed in xml 1.0
     */
    static String xmlText(final String text) {
        StringBuilder xmlText = new StringBuilder(text.length());
        text.codePoints()
            .filter(codePoint -> codePoint == 0x9 || codePoint == 0xA || codePoint == 0xD ||
                codePoint >= 0x20 && codePoint <= 0xD7FF ||
                codePoint >= 0xE000 && codePoint <= 0xFFFD ||
                codePoint >= 0x10000 && codePoint <= 0x10FFFF)
            .forEach(xmlText::appendCodePoint);
        return xmlText.toString();
    }
}
