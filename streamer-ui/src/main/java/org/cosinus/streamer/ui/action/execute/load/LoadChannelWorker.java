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

package org.cosinus.streamer.ui.action.execute.load;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.worker.ChannelWorker;

public class LoadChannelWorker extends ChannelWorker {

    private final LoadActionModel<?> loadActionModel;

    protected LoadChannelWorker(LoadActionModel<?> loadActionModel) {
        super(loadActionModel,
            loadActionModel.getStreamerViewToLoadTo().getLoadWorkerModel(),
            loadActionModel.getStreamerViewToLoadTo().getLoadingIndicator().getProgressModel(),
            loadActionModel.getStreamerToLoad().binaryStreamer());

        this.loadActionModel = loadActionModel;
    }

    @Override
    protected void doWork() {
        loadActionModel.getStreamerToLoad().init();
        loadActionModel.getStreamerViewToLoadTo().reset((Streamer) loadActionModel.getStreamerToLoad());
        super.doWork();
    }

    @Override
    protected long offset() {
        return loadActionModel.getOffset();
    }

    @Override
    protected long limit() {
        long limit = loadActionModel.getLimit();
        return limit >= 0 ? limit : DEFAULT_BUFFER_SIZE;
    }
}
