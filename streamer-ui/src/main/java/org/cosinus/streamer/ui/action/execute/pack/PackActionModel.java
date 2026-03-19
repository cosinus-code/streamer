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
package org.cosinus.streamer.ui.action.execute.pack;

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.view.StreamerView;

import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.PackStreamerAction.PACK_STREAMER_ACTION_ID;

@Getter
public class PackActionModel extends CopyActionModel {

    public static final String PACK_TYPE = "packType";

    public static final String PACK_TO = "packTo";

    public static final String PACK_FILTER = "packFilter";

    public static final Set<String> PACK_KEYS = Set.of(PACK_TYPE, PACK_TO, PACK_FILTER);

    private String packType;

    private String[] packTypes;

    public static PackActionModel pack(List<?> streamersToCopy) {
        return new PackActionModel()
            .streamers(streamersToCopy);
    }

    @Override
    public PackActionModel streamers(List<?> streamersToCopy) {
        super.streamers(streamersToCopy);
        return this;
    }

    @Override
    public PackActionModel from(StreamerView<?> sourceView) {
        super.from(sourceView);
        return this;
    }

    @Override
    public PackActionModel from(ParentStreamer<?> source) {
        super.from(source);
        return this;
    }

    @Override
    public PackActionModel to(ParentStreamer<?> destination, StreamerView<?> destinationView) {
        super.to(destination, destinationView);
        return this;
    }

    @Override
    public PackActionModel to(StreamerView<?> destinationView) {
        super.to(destinationView);
        return this;
    }

    @Override
    public PackActionModel to(ParentStreamer<?> destination) {
        super.to(destination);
        return this;
    }

    public PackActionModel as(String packType) {
        this.packType = packType;
        return this;
    }

    public PackActionModel packTypes(String[] packTypes) {
        this.packTypes = packTypes;
        return this;
    }

    @Override
    public Set<String> keys() {
        return PACK_KEYS;
    }

    @Override
    public Object getValue(String key) {
        if (key.equals(PACK_TO)) {
            return getTargetPath().toFile();
        }

        if (key.equals(PACK_TYPE)) {
            return packType;
        }

        return null;
    }

    @Override
    public void putValue(String key, Object value) {
        ofNullable(value)
            .map(Object::toString)
            .ifPresent(text -> {
                switch (key) {
                    case PACK_TO -> setTargetPath(text);
                    case PACK_TYPE -> packType = text;
                    case PACK_FILTER -> setCopyFilter(text);
                }
            });
    }

    @Override
    public Object[] getValues(String key) {
        return packTypes;
    }

    @Override
    public String getActionId() {
        return PACK_STREAMER_ACTION_ID;
    }
}
