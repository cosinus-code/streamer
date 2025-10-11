/*
 * Copyright 2025 Cosinus Software
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
package org.cosinus.streamer.api.worker;

import org.cosinus.stream.consumer.StreamConsumer;

import java.util.stream.Stream;

public interface SaveWorkerModel<T> extends WorkerModel<T> {

    Stream<T> streamToSave();

    StreamConsumer<T> streamConsumer();

    long totalItemsToSave();

    long getSavedItemsCount();

    void setDirty(boolean dirty);

    //TODO: should be taken from org.cosinus.streamer.ui.view.StreamerEditor
    boolean isDirty();
}
