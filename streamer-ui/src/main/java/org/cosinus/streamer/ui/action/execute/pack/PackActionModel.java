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
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;
import org.cosinus.swing.ui.UIModel;

import java.util.Set;

import static java.util.Optional.ofNullable;

@Getter
public class PackActionModel<S extends Streamer<S>, T extends Streamer<T>>
    extends CopyActionModel<S, T> implements UIModel {

    public static final String PACK_TYPE = "packType";

    public static final String PACK_TO = "packTo";

    public static final String PACK_FILTER = "packFilter";

    public static final Set<String> PACK_KEYS = Set.of(PACK_TYPE, PACK_TO, PACK_FILTER);

    private String packType;

    @Setter
    private String[] packTypes;

    public PackActionModel(String actionId, String actionName) {
        super(actionId, actionName);
    }

    public static <S extends Streamer<S>, T extends Streamer<T>>
    PackActionModel<S, T> pack(String actionId, String actionName, ParentStreamerViewContext<S> from, ParentStreamerViewContext<T> to) {
        PackActionModel<S, T> packActionModel = new PackActionModel<>(actionId, actionName);
        packActionModel
            .setStreamersToCopy(from.getSelectedItems())
            .from(from.getStreamerView(), from.getParentStreamer())
            .to(to.getStreamerView(), to.getParentStreamer());
        return packActionModel;
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
}
