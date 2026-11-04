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

package org.cosinus.streamer.api.transform.xml;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.transform.TransformBinaryStreamer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

/**
 * {@link TransformBinaryStreamer} that converts xml content into json content.
 * <p>
 * The document is written as an object with a single member, named as the root element.
 * An element is written as:
 * <ul>
 *     <li>its text, as a string, if it has no attributes and no child elements
 *     (an empty string for an empty element);</li>
 *     <li>an object otherwise, with a member for each attribute, named as the attribute,
 *     a member for each child element, named as the element,
 *     with an array of their values when several child elements (or an attribute and child elements)
 *     have the same name, the attribute first,
 *     and a "#text" member for its text, if it has any besides the child elements.</li>
 * </ul>
 * The texts are trimmed, the values are always strings (e.g. "007" stays as it is),
 * the names are kept as they are written (with their namespace prefix),
 * and the comments and the processing instructions are skipped.
 * <p>
 * The encoding of the xml is detected by the parser (byte order mark or xml declaration).
 * The internal entities are expanded, within the limits of the secure processing of the parser,
 * but no external entity or dtd is loaded, so the content cannot read other files or urls.
 * The json is written in utf-8, indented.
 */
public class XmlToJsonBinaryStreamer extends TransformBinaryStreamer<Document> {

    static final String TEXT = "#text";

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build();

    /**
     * Throws the errors, instead of printing them on the console as the default error handler does.
     */
    private static final ErrorHandler THROWING_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void warning(final SAXParseException exception) {
        }

        @Override
        public void error(final SAXParseException exception) throws SAXException {
            throw exception;
        }

        @Override
        public void fatalError(final SAXParseException exception) throws SAXException {
            throw exception;
        }
    };

    public XmlToJsonBinaryStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
    }

    @Override
    protected Document read(final InputStream input) throws IOException {
        try {
            DocumentBuilder builder = documentBuilderFactory().newDocumentBuilder();
            builder.setErrorHandler(THROWING_ERROR_HANDLER);
            return builder.parse(input);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Invalid xml content: " + e.getMessage(), e);
        }
    }

    @Override
    protected void write(final Document document, final OutputStream output) throws IOException {
        try {
            JSON_MAPPER.writeValue(output, json(document));
        } catch (JacksonException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return the json of the document: an object with the root element
     */
    static ObjectNode json(final Document document) {
        Element root = document.getDocumentElement();
        ObjectNode json = JSON_MAPPER.createObjectNode();
        json.set(root.getNodeName(), value(root));
        return json;
    }

    private static JsonNode value(final Element element) {
        ObjectNode object = JSON_MAPPER.createObjectNode();
        NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            object.put(attribute.getNodeName(), attribute.getNodeValue());
        }

        StringBuilder text = new StringBuilder();
        boolean hasChildElements = false;
        NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE -> {
                    hasChildElements = true;
                    addMember(object, child.getNodeName(), value((Element) child));
                }
                case Node.TEXT_NODE, Node.CDATA_SECTION_NODE ->
                    text.append(child.getTextContent());
                default -> {
                    // comments and processing instructions are skipped
                }
            }
        }

        String trimmed = text.toString().strip();
        if (!hasChildElements && object.isEmpty()) {
            return StringNode.valueOf(trimmed);
        }
        if (!trimmed.isEmpty()) {
            object.put(TEXT, trimmed);
        }
        return object;
    }

    /**
     * The values with the same name (of child elements, or of an attribute and child elements)
     * are put in an array, in their order.
     */
    private static void addMember(final ObjectNode object, final String name, final JsonNode value) {
        JsonNode existing = object.get(name);
        if (existing == null) {
            object.set(name, value);
        } else if (existing instanceof ArrayNode array) {
            array.add(value);
        } else {
            ArrayNode array = JSON_MAPPER.createArrayNode();
            array.add(existing);
            array.add(value);
            object.set(name, array);
        }
    }

    /**
     * @return a factory of parsers that don't load external entities or dtds (to avoid xxe attacks),
     * and that limit the entity expansions
     */
    static DocumentBuilderFactory documentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setNamespaceAware(false);
        return factory;
    }
}
