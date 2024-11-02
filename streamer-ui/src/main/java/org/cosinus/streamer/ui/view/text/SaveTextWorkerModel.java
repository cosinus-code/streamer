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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.worker.AbstractSaveWorkerModel;

import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.LongStream.range;

public class SaveTextWorkerModel extends AbstractSaveWorkerModel<String> {

    private final TextStreamerEditor textEditor;

    public SaveTextWorkerModel(TextStreamerEditor textEditor) {
        this.textEditor = textEditor;
    }

    @Override
    public Stream<String> streamToSave() {
        this.savedItemsCount = 0;
        return range(0, totalItemsToSave())
            .boxed()
            .mapToInt(Long::intValue)
            .mapToObj(textEditor::getLineAtIndex);
    }

    @Override
    public StreamConsumer<String> streamConsumer() {
        return ofNullable(textEditor.getParentStreamer())
            .map(Streamer::streamConsumer)
            .orElse(null);
    }

    @Override
    public long totalItemsToSave() {
        return textEditor.getLineCount();
    }

    @Override
    public void setDirty(boolean dirty) {
        textEditor.setDirty(dirty);
    }

    @Override
    public boolean isDirty() {
        return textEditor.isDirty();
    }
}
