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
package org.cosinus.streamer.api.transform.html;

/**
 * Helpers to write a standalone html5 page in utf-8.
 */
public final class HtmlPage {

    private HtmlPage() {
    }

    /**
     * @param title the title of the page, as text
     * @param style the css of the page
     * @param body  the html of the body
     * @return the html page
     */
    public static String page(final String title, final String style, final String body) {
        return "<!DOCTYPE html>\n" +
            "<html>\n<head>\n<meta charset=\"utf-8\">\n" +
            "<title>" + escape(title) + "</title>\n" +
            "<style>" + style + "</style>\n" +
            "</head>\n<body>\n" +
            body +
            "</body>\n</html>\n";
    }

    /**
     * @return the name of the file without its extension, or an empty title if there is no name
     */
    public static String title(final String name) {
        if (name == null) {
            return "";
        }
        int extension = name.lastIndexOf('.');
        return extension > 0 ? name.substring(0, extension) : name;
    }

    /**
     * @return the text, with the html special characters escaped, so that it is shown as it is
     */
    public static String escape(final String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char character : text.toCharArray()) {
            switch (character) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                default -> escaped.append(character);
            }
        }
        return escaped.toString();
    }
}
