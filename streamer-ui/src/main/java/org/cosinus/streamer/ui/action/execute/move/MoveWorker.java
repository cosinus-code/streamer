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

import java.util.List;

import static org.cosinus.streamer.api.stream.FlatStreamingStrategy.LEVEL_BOTTOM_UP;

public class MoveWorker<S extends Streamer<S>, T extends Streamer<T>> extends CopyWorker<S, T> {

    private List<S> streamersToCopy;

    public MoveWorker(CopyActionModel<S, T> copyModel) {
        super(copyModel);
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
    protected void copyStreamer(S streamerToCopy, T streamerToCopyTo) {
        super.copyStreamer(streamerToCopy, streamerToCopyTo);
        if (!streamerToCopy.isParent()) {
            streamerToCopy.delete();
        }
    }

    @Override
    protected void onWorkerDoneBeforeFinishing() {
        super.onWorkerDoneBeforeFinishing();
        source.flatStream(LEVEL_BOTTOM_UP, getStreamerFilter())
            .filter(Streamer::exists)
            .forEach(Streamer::delete);
    }
}
