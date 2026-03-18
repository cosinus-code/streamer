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

package org.cosinus.streamer.ui.action.execute.copy;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.swing.progress.ProgressModel;

/**
 * Model for a progress over multiple streamers for actions with source and target
 */
public class CopyProgressModel<S extends Streamer<?>, T extends Streamer<?>> extends ProgressModel {

    private final ProgressModel streamerProgressModel;

    @Setter
    @Getter
    private long totalItems;

    @Getter
    private S source;

    @Getter
    private T target;

    public CopyProgressModel() {
        streamerProgressModel = new ProgressModel();
    }

    public int getStreamerProgress() {
        return streamerProgressModel.getProgressPercent();
    }

    public void startStreamerProgress(S source, T target) {
        streamerProgressModel.startProgress(source.getSize());
        this.source = source;
        this.target = target;
    }

    public void updateStreamerProgress(long value) {
        streamerProgressModel.addProgress(value);
        addProgress(value);
    }

    public void finishStreamerProgress() {
        streamerProgressModel.finishProgress();
    }
}
