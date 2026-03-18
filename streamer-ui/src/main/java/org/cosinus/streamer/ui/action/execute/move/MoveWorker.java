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

package org.cosinus.streamer.ui.action.execute.move;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorker;
import org.cosinus.streamer.ui.action.execute.copy.CopyUnit;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.cosinus.stream.FlatStreamingStrategy.LEVEL_BOTTOM_UP;

public class MoveWorker<S extends Streamer<?>, T extends Streamer<?>> extends CopyWorker<S, T> {

    private List<S> streamersToCopy;

    private final MoveWorkerModel<S, T> moveWorkerModel;

    public MoveWorker(CopyActionModel moveActionModel, MoveWorkerModel<S, T> moveWorkerModel) {
        super(moveActionModel, moveWorkerModel);
        this.moveWorkerModel = moveWorkerModel;
    }

    @Override
    public StreamerFilter getStreamerFilter() {
        return streamersToCopy::contains;
    }

    @Override
    protected void doWork() {
        this.streamersToCopy = source
            .stream()
            .filter(streamerFilter)
            .filter(streamerToMove -> !streamerToMove.rename(targetStreamer(streamerToMove).getPath()))
            .toList();
        if (!this.streamersToCopy.isEmpty()) {
            super.doWork();
        }
    }

    @Override
    protected void copyStreamer(CopyUnit<S, T> copyUnit) {
        super.copyStreamer(copyUnit);
        S streamerToMove = copyUnit.source();
        if (!streamerToMove.isParent()) {
            streamerToMove.delete(false);
            moveWorkerModel.updateDeletedItems(singletonList(streamerToMove));
        }
    }

    @Override
    protected void onWorkerDoneBeforeFinishing() {
        super.onWorkerDoneBeforeFinishing();
        if (isSuccessful()) {
            source.flatStream(LEVEL_BOTTOM_UP, copyStrategy, getStreamerFilter())
                .filter(Streamer::exists)
                .forEach(streamer -> {
                    streamer.delete(false);
                    moveWorkerModel.updateDeletedItems(singletonList(streamer));
                });
        }
    }
}
