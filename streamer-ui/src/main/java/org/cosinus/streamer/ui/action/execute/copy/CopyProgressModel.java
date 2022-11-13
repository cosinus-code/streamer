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

package org.cosinus.streamer.ui.action.execute.copy;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.progress.ProgressModel;
import org.cosinus.streamer.ui.action.progress.SimpleProgressModel;

/**
 * Model for a progress over multiple streamers for actions with source and target
 */
public class CopyProgressModel implements ProgressModel {

    private final SimpleProgressModel totalProgressModel;

    private final SimpleProgressModel streamerProgressModel;

    private final String actionId;

    private Streamer<?> source;

    private Streamer<?> target;

    public CopyProgressModel(String actionId) {
        this.actionId = actionId;
        totalProgressModel = new SimpleProgressModel(actionId);
        streamerProgressModel = new SimpleProgressModel(actionId);
    }

    @Override
    public String getActionId() {
        return actionId;
    }

    public int getTotalProgress() {
        return totalProgressModel.getProgress();
    }

    public int getStreamerProgress() {
        return streamerProgressModel.getProgress();
    }

    public long getSpeed() {
        return totalProgressModel.getSpeed();
    }

    public long getRemainingTime() {
        return totalProgressModel.getRemainingTime();
    }

    public void startTotalProgress(long totalProgressSize) {
        totalProgressModel.startProgress(totalProgressSize);
    }

    public void startStreamerProgress(Streamer<?> source, Streamer<?> target) {
        streamerProgressModel.startProgress(source.getSize());
        this.source = source;
        this.target = target;
    }

    public void updateTotalProgress(long value) {
        totalProgressModel.updateProgress(value);
    }

    public void updateStreamerProgress(long value) {
        streamerProgressModel.updateProgress(value);
        updateTotalProgress(value);
    }

    public void finishStreamerProgress() {
        streamerProgressModel.finishProgress();
    }

    public void finishTotalProgress() {
        totalProgressModel.finishProgress();
    }

    public Streamer<?> getSource() {
        return source;
    }

    public Streamer<?> getTarget() {
        return target;
    }
}
