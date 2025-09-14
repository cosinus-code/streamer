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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TextValue;

import static java.util.Collections.singletonList;

public abstract class StravaParentStreamer<S extends Streamer>
    extends StravaStreamer<S> implements ParentStreamer<S> {

    protected final String folderName;

    public StravaParentStreamer(final StravaUserStreamer stravaUserStreamer,
                                final String folderName) {
        super(stravaUserStreamer);
        this.folderName = folderName;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaUserStreamer;
    }

    @Override
    public String getName() {
        return folderName;
    }

    @Override
    public String getIconName() {
        return folderName;
    }

    @Override
    public void reset() {
        details = singletonList(new TextValue(getName()));
    }
}
