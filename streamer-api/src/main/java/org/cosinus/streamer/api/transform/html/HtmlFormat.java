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

import java.util.Set;

/**
 * The html formats, as source or target of a transformation.
 */
public final class HtmlFormat {

    public static final String HTML_FORMAT = "html";

    public static final String HTM_FORMAT = "htm";

    public static final Set<String> HTML_FORMATS = Set.of(HTML_FORMAT, HTM_FORMAT);

    private HtmlFormat() {
    }
}
