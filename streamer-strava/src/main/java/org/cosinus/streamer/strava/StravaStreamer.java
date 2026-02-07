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

package org.cosinus.streamer.strava;

import lombok.Getter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.strava.client.StravaCacheClient;
import org.cosinus.streamer.strava.client.StravaClientInvoker;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public abstract class StravaStreamer<T> implements Streamer<T> {

    @Autowired
    protected StravaClientInvoker stravaClientInvoker;

    @Getter
    protected final StravaUserStreamer stravaUserStreamer;

    protected final String userName;

    protected List<Value> details;

    protected StravaStreamer(final StravaUserStreamer stravaUserStreamer) {
        injectContext(this);
        this.stravaUserStreamer = stravaUserStreamer;
        this.userName = stravaUserStreamer.getName();
    }

    protected <V> V invokeStravaClient(Function<StravaCacheClient, V> stravaClientCall) {
        return stravaClientInvoker.invoke(userName, stravaClientCall);
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(getName());
    }

    @Override
    public String getProtocol() {
        return stravaUserStreamer.getProtocol();
    }

    @Override
    public List<Value> details() {
        if (details == null) {
            initDetails();
        }
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StravaStreamer that)) {
            return false;
        }
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    protected abstract void initDetails();
}
