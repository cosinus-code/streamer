/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;

import java.util.List;

/**
 * Data model
 */
public class StreamedContent<T> {

    private final Streamer<T> streamer;

    private final Streamer<T> contentToSelect;

    private final List<T> content;

    public StreamedContent(Streamer<T> streamer,
                           List<T> content,
                           Streamer<T> contentToSelect) {
        this.streamer = streamer;
        this.content = content;
        this.contentToSelect = contentToSelect;
    }

    public Streamer<T> getStreamer() {
        return streamer;
    }

    public Streamer<T> getContentToSelect() {
        return contentToSelect;
    }

    public List<T> getContent() {
        return content;
    }
}
