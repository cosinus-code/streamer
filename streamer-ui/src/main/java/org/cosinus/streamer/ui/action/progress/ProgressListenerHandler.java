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

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

/**
 * Handler for progress listeners
 */
@Component
public class ProgressListenerHandler<P extends ProgressModel> {

    private final Map<String, Queue<ProgressListener<P>>> progressListenersMap = new HashMap<>();

    public void register(String actionId, ProgressListener<P> listener) {
        progressListenersMap
            .computeIfAbsent(actionId, k -> new ConcurrentLinkedQueue<>())
            .add(listener);
    }

    public void startProgress(String actionId) {
        getListeners(actionId)
            .forEach(ProgressListener::startProgress);
    }

    public void setProgress(P progress) {
        getListeners(progress.getActionId())
            .forEach(listener -> listener.setProgress(progress));
    }

    public void finishProgress(P progress) {
        ofNullable(progressListenersMap.get(progress.getActionId()))
            .ifPresent(this::finishProgress);
    }

    private void finishProgress(Collection<ProgressListener<P>> listeners) {
        listeners.forEach(ProgressListener::finishProgress);
        listeners.clear();
    }

    private Stream<ProgressListener<P>> getListeners(String actionId) {
        return ofNullable(progressListenersMap.get(actionId))
            .stream()
            .flatMap(Collection::stream);
    }
}
