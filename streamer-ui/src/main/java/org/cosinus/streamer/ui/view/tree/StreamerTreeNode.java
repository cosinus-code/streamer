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

package org.cosinus.streamer.ui.view.tree;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.BinaryExpanderHandler;
import org.cosinus.swing.translate.Translator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.apache.commons.lang3.ObjectUtils.compare;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StreamerTreeNode extends DefaultMutableTreeNode implements Comparable<StreamerTreeNode> {

    public static final String TREE_NODE_LOADING = "tree-node-loading";

    @Autowired
    private Translator translator;

    @Autowired
    private BinaryExpanderHandler expanderHandler;

    @Getter
    @Setter
    private boolean loaded;

    @Getter
    @Setter
    private boolean loading;

    public StreamerTreeNode(final Streamer<?> streamer, boolean canExpand) {
        super(streamer, canExpand);
        injectContext(this);
    }

    public Streamer<?> getStreamer() {
        return (Streamer<?>) userObject;
    }

    public void loadChildren() {
        removeAllChildren();

        Streamer<?> streamer = ofNullable(expanderHandler.expandStreamer(getStreamer()))
            .orElse(getStreamer());
        streamer
            .stream()
            .filter(item -> Streamer.class.isAssignableFrom(item.getClass()))
            .map(Streamer.class::cast)
            .sorted()
            .map(this::createLoadingNode)
            .forEach(this::add);
        loaded = true;
    }

    public StreamerTreeNode createLoadingNode(Streamer<?> streamer) {
        boolean canExpand = streamer.isParent() || expanderHandler.findStreamExpander(streamer.getType()).isPresent();
        StreamerTreeNode loadingNode = new StreamerTreeNode(streamer, canExpand);
        if (streamer.isParent()) {
            loadingNode.add(new DefaultMutableTreeNode(translator.translate(TREE_NODE_LOADING)));
        }
        return loadingNode;
    }

    public void sort() {
        List<StreamerTreeNode> children = range(0, getChildCount())
            .mapToObj(this::getChildAt)
            .map(StreamerTreeNode.class::cast)
            .sorted()
            .toList();

        removeAllChildren();
        children.forEach(this::add);
    }

    public Stream<StreamerTreeNode> childStream() {
        return range(0, getChildCount())
            .mapToObj(this::getChildAt)
            .filter(StreamerTreeNode.class::isInstance)
            .map(StreamerTreeNode.class::cast);
    }

    @Override
    public int compareTo(@NotNull StreamerTreeNode other) {
        return compare(getStreamer(), other.getStreamer());
    }
}
