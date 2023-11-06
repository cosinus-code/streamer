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

package org.cosinus.streamer.ui.action.execute;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

/**
 * Handler for worker listeners
 */
@Component
public class WorkerListenerHandler<M extends WorkerModel> {

    private final Map<String, Queue<WorkerListener<M>>> workerListenersMap = new HashMap<>();

    /**
     * Register a worker listener.
     *
     * @param workerId the worker id
     * @param listener the worker listener
     */
    public void register(String workerId, final WorkerListener<M>... listener) {
        workerListenersMap
            .computeIfAbsent(workerId, k -> new ConcurrentLinkedQueue<>())
            .addAll(asList(listener));
    }

    /**
     * Signal a worker was tarted.
     *
     * @param workerId the worker id
     */
    public void workerStarted(String workerId, M workerModel) {
        getListeners(workerId)
            .forEach(listener -> listener.workerStarted(workerModel));
    }

    /**
     * Signal a worker model was updated.
     *
     * @param workerModel the worker model
     */
    public void workerUpdated(String workerId, M workerModel) {
        getListeners(workerId)
            .forEach(listener -> listener.workerUpdated(workerModel));
    }

    /**
     * Signal a worker was finished.
     *
     * @param workerModel the worker model
     */
    public void workerFinished(String workerId, M workerModel) {
        ofNullable(workerListenersMap.get(workerId))
            .ifPresent(listeners -> workerFinished(listeners, workerModel));
    }

    /**
     * Signal to listeners that a worker was finished.
     *
     * @param listeners the listeners
     */
    private void workerFinished(Collection<WorkerListener<M>> listeners, M workerModel) {
        listeners.forEach(listener -> listener.workerFinished(workerModel));
        listeners.clear();
    }

    /**
     * Get the listeners registered to listen for a worker id.
     *
     * @param workerId the worker is
     * @return the stream of listeners
     */
    private Stream<WorkerListener<M>> getListeners(String workerId) {
        return ofNullable(workerListenersMap.get(workerId))
            .stream()
            .flatMap(Collection::stream);
    }
}
