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
package org.cosinus.streamer.ui.view.text;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.stream.text.TextStreamConsumer;
import org.cosinus.streamer.ui.action.execute.save.SaveWorkerModel;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;

public class SaveTextModel implements SaveWorkerModel<String> {

    private final TextEditor textEditor;

    private int savedItemsCount;

    public SaveTextModel(TextEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public Stream<String> streamToSave() {
        this.savedItemsCount = 0;
        return range(0, totalItemsToSave())
            .mapToObj(textEditor::getLineAtIndex);
    }

    @Override
    public StreamConsumer<String> saveConsumer() {
        return ofNullable(textEditor.getParentStreamer())
            .map(Streamer::binaryStreamer)
            .map(binaryStreamer -> binaryStreamer.outputStream(false))
            .map(TextStreamConsumer::new)
            .orElse(null);
    }

    @Override
    public int totalItemsToSave() {
        return textEditor.getLineCount();
    }

    @Override
    public void update(List<String> items)
    {
        savedItemsCount += items.size();
    }

    public int getSavedItemsCount()
    {
        return savedItemsCount;
    }
}
