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

package org.cosinus.streamer.ui.view.binary;

import org.cosinus.stream.consumer.StreamConsumer;
import org.cosinus.streamer.api.worker.AbstractSaveWorkerModel;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.stream.Stream;

public class BinarySaveModel extends AbstractSaveWorkerModel<BinarySaveUnit> {

    private final BinaryHexaEditor binaryEditor;

    public BinarySaveModel(final BinaryHexaEditor binaryEditor) {
        this.binaryEditor = binaryEditor;
    }

    @Override
    public Stream<BinarySaveUnit> streamToSave() {
        return binaryEditor.getEditedBytes()
            .entrySet()
            .stream()
            .map(entry -> new BinarySaveUnit(entry.getKey(), new byte[]{entry.getValue()}));
    }

    @Override
    public StreamConsumer<BinarySaveUnit> streamConsumer() {
        return saveUnit -> {
            try {
                FileChannel fileChannel = binaryEditor.getBinaryStreamer().fileChannel();
                fileChannel.position(saveUnit.getPosition());
                fileChannel.write(ByteBuffer.wrap(saveUnit.getBuffer()));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Override
    public long totalItemsToSave() {
        return binaryEditor.getEditedBytes().size();
    }

    @Override
    public void setDirty(boolean dirty) {
        binaryEditor.setDirty(dirty);
    }

    @Override
    public boolean isDirty() {
        return binaryEditor.isDirty();
    }
}
