/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.worker.WorkerModel;

import java.util.List;

public class CopyWorkerModel<S extends Streamer<?>, T extends Streamer<?>>
    implements WorkerModel<CopyUnit<S, T>> {

    private final WorkerModel<T> copyWorkerModel;

    public CopyWorkerModel(final WorkerModel copyWorkerModel) {
        this.copyWorkerModel = copyWorkerModel;
    }

    @Override
    public void update(List<CopyUnit<S, T>> items) {
        copyWorkerModel.update(items
            .stream()
            .map(CopyUnit::target)
            .toList());
    }
}
