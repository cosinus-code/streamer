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

import org.cosinus.swing.context.SpringSwingComponent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handler for progress listeners
 */
@SpringSwingComponent
public class ProgressListenerHandler<P extends ProgressModel> {

    //TODO: to groups listeners by action id
    private final Queue<ProgressListener<P>> progressListeners = new ConcurrentLinkedQueue<>();

    public void register(ProgressListener<P> listener) {
        progressListeners.add(listener);
    }

    public void startProgress() {
        progressListeners
                .forEach(ProgressListener::startProgress);
    }

    public void setProgress(P progress) {
        progressListeners
                .forEach(listener -> listener.setProgress(progress));
    }

    public void finishProgress() {
        progressListeners
                .forEach(ProgressListener::finishProgress);
        progressListeners.clear();
    }
}
