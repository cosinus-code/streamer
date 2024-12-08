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

package org.cosinus.streamer.ui.action.progress;

import org.cosinus.streamer.api.worker.WorkerModel;

import java.util.List;

/**
 * Action progress model
 */
public class SimpleProgressModel implements WorkerModel<Long> {

    private long progressTotalSize;

    private long progressDone;

    private int progressPercent;

    private long startTime;

    private long speed;

    private long remainingTime;

    @Override
    public void update(List<Long> items) {
        updateProgress(items
            .stream()
            .mapToLong(Long::longValue)
            .sum());
    }

    public void startProgress(long totalProgressSize) {
        startTime = System.currentTimeMillis();
        this.progressTotalSize = totalProgressSize;
        this.progressDone = 0;
        this.progressPercent = 0;
    }

    public void updateProgress(long value) {
        progressDone += value;
        if (progressTotalSize != 0) {
            progressPercent = (int) ((progressDone * 100) / progressTotalSize);
        }

        long spentTime = System.currentTimeMillis() - startTime;
        if (spentTime > 0) {
            speed = 1000 * progressDone / spentTime;
            remainingTime = progressDone != 0 ?
                (progressTotalSize - progressDone) * spentTime / (1000 * progressDone) :
                0;
        }
    }

    public void finishProgress() {
        progressPercent = 100;
    }

    public int getProgress() {
        return progressPercent;
    }

    public long getSpeed() {
        return speed;
    }

    public long getRemainingTime() {
        return remainingTime;
    }
}
