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
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.ui.view.ParentStreamerViewContext;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.ui.UIModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.not;

/**
 * Encapsulates the model of the copy streamers action
 */
public class CopyActionModel<S extends Streamer<S>, T extends Streamer<T>> extends ActionModel implements UIModel {

    public static final String COPY_TO = "copyTo";

    public static final String COPY_FILTER = "copyFilter";

    public static final Set<String> COPY_KEYS = Set.of(COPY_TO, COPY_FILTER);

    @Getter
    private StreamerFilter sourceFilter = streamer -> true;

    @Getter
    private StreamerView<S, S> streamerViewSource;

    @Getter
    private StreamerView<T, T> streamerViewTarget;

    @Getter
    private List<Streamer<S>> streamersToCopy;

    @Getter
    private ParentStreamer<S> source;

    @Getter
    private ParentStreamer<T> destination;

    @Getter
    private Path targetPath;

    public CopyActionModel(String actionId, String actionName) {
        super(randomUUID().toString(), actionId, actionName);
    }

    public static <S extends Streamer<S>, T extends Streamer<T>>
    CopyActionModel<S, T> copy(String actionId, String actionName, ParentStreamerViewContext<S> from, ParentStreamerViewContext<T> to) {
        return new CopyActionModel<S, T>(actionId, actionName)
            .setStreamersToCopy(from.getSelectedItems())
            .from(from.getStreamerView(), from.getParentStreamer())
            .to(to.getStreamerView(), to.getParentStreamer());
    }

    public void setTargetPath(Path targetPath) {
        this.targetPath = targetPath;
    }

    public void setTargetPath(String targetPath) {
        setTargetPath(Paths.get(targetPath));
    }

    public void setCopyFilter(String pattern) {
        String regularExpression = "^" + pattern
            .replace(".", "\\.")
            .replace("*", ".*") + "$";
        sourceFilter = streamer -> streamersToCopy.contains(streamer)
            && streamer.getName().matches(regularExpression);

    }

    public CopyActionModel<S, T> setStreamersToCopy(List<Streamer<S>> streamersToCopy) {
        this.streamersToCopy = streamersToCopy;
        this.sourceFilter = this.streamersToCopy::contains;
        return this;
    }

    public CopyActionModel<S, T> from(StreamerView<S, S> streamerViewSource, ParentStreamer<S> source) {
        this.streamerViewSource = streamerViewSource;
        this.source = source;
        return this;
    }

    public CopyActionModel<S, T> to(StreamerView<T, T> streamerViewTarget, ParentStreamer<T> destination) {
        this.streamerViewTarget = streamerViewTarget;
        this.destination = destination;
        this.targetPath = destination.getPath();
        return this;
    }

    public CopyActionModel<S, T> to(ParentStreamer<T> destination) {
        this.destination = destination;
        this.targetPath = destination.getPath();
        return this;
    }

    @Override
    public Set<String> keys() {
        return COPY_KEYS;
    }

    @Override
    public void putValue(String key, Object value) {
        ofNullable(value)
            .map(Object::toString)
            .filter(not(String::isBlank))
            .ifPresent(text -> {
                if (key.equals(COPY_TO)) {
                    setTargetPath(text);
                } else if (key.equals(COPY_FILTER)) {
                    setCopyFilter(text);
                }
            });
    }

    @Override
    public Object getValue(String key) {
        return key.equals(COPY_TO) ? getTargetPath().toFile() : null;
    }
}
