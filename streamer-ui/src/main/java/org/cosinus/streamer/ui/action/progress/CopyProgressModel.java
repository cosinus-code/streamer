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

import org.cosinus.streamer.api.Streamer;

/**
 * Model for a progress over multiple elements for actions with source and target
 */
public class CopyProgressModel implements ProgressModel {

    private final SimpleProgressModel totalProgressModel;

    private final SimpleProgressModel elementProgressModel;

    private Streamer source;

    private Streamer target;

    public CopyProgressModel() {
        totalProgressModel = new SimpleProgressModel();
        elementProgressModel = new SimpleProgressModel();
    }

    public int getTotalProgress() {
        return totalProgressModel.getProgress();
    }

    public int getElementProgress() {
        return elementProgressModel.getProgress();
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

    public void startElementProgress(Streamer source, Streamer target) {
        elementProgressModel.startProgress(source.getSize());
        this.source = source;
        this.target = target;
    }

    public void updateTotalProgress(long value) {
        totalProgressModel.updateProgress(value);
    }

    public void updateElementProgress(long value) {
        elementProgressModel.updateProgress(value);
        updateTotalProgress(value);
    }

    public void finishElementProgress() {
        elementProgressModel.finishProgress();
    }

    public void finishTotalProgress() {
        totalProgressModel.finishProgress();
    }

    public Streamer getSource() {
        return source;
    }

    public Streamer getTarget() {
        return target;
    }
}
