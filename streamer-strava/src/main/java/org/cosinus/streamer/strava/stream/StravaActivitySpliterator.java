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

package org.cosinus.streamer.strava.stream;

import org.cosinus.streamer.strava.client.StravaClientInvoker;
import org.cosinus.streamer.strava.model.Activity;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;

import static java.lang.Long.MAX_VALUE;

public class StravaActivitySpliterator implements Spliterator<Activity> {

    private static final int PAGE_SIZE = 200;

    private final String userId;

    private final StravaClientInvoker stravaClientInvoker;

    private final Queue<Activity> activities;

    private final long startTime;

    private final long endTime;

    private int page = 1;

    public StravaActivitySpliterator(final String userId,
                                     final StravaClientInvoker stravaClientInvoker,
                                     final long startTime,
                                     final long endTime) {
        this.userId = userId;
        this.stravaClientInvoker = stravaClientInvoker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activities = new LinkedList<>();
    }

    @Override
    public boolean tryAdvance(final Consumer<? super Activity> action) {
        if (activities.isEmpty()) {
            activities.addAll(getActivities());
        }

        if (activities.isEmpty()) {
            return false;
        }

        action.accept(activities.poll());
        return true;
    }

    @Override
    public Spliterator<Activity> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL;
    }

    protected List<Activity> getActivities() {
        return stravaClientInvoker.invoke(userId, stravaClient ->
            stravaClient.getActivities(startTime, endTime, PAGE_SIZE, page++));
    }
}
