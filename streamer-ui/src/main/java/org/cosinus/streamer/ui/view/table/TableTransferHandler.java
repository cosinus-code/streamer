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

package org.cosinus.streamer.ui.view.table;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.file.PathListTransferable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.awt.datatransfer.DataFlavor.javaFileListFlavor;
import static java.awt.datatransfer.DataFlavor.stringFlavor;
import static java.lang.System.lineSeparator;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.file.PathListTransferable.PATH_FLAVORS;
import static org.cosinus.swing.file.PathListTransferable.PATH_FLAVOR;

@Slf4j
public class TableTransferHandler<T extends Streamable> extends TransferHandler {

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    private final TableStreamerView<T> view;

    public TableTransferHandler(final TableStreamerView<T> view) {
        injectContext(this);
        this.view = view;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        streamerViewHandler.setDragAndDropSourceView(view);
        return new PathListTransferable(view.getSelectedItems()
            .stream()
            .map(Streamable::getPath)
            .filter(Objects::nonNull)
            .toList());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferSupport support) {
        AtomicBoolean imported = new AtomicBoolean(false);
        if (support.getComponent() instanceof JComponent component) {
            Transferable transferable = support.getTransferable();
            tryGetPaths(transferable, PATH_FLAVOR)
                .or(() -> tryGetPaths(transferable, javaFileListFlavor))
                .or(() -> tryGetPaths(transferable, stringFlavor))
                .filter(not(CollectionUtils::isEmpty))
                .ifPresent(paths -> {
                    Point dropPoint = support.getDropLocation().getDropPoint();
                    view.showDragAndDropPopup(component, dropPoint, paths);
                    imported.set(true);
                });
        }

        return imported.get();
    }

    @Override
    public boolean canImport(TransferSupport support) {
        Transferable transferable = support.getTransferable();
        return stream(transferable.getTransferDataFlavors())
            .anyMatch(flavor -> stream(PATH_FLAVORS).anyMatch(flavor::equals));
    }

    protected Optional<List<String>> tryGetPaths(final Transferable transferable, final DataFlavor flavor) {
        try {
            if (transferable.isDataFlavorSupported(flavor)) {
                Object data = transferable.getTransferData(flavor);
                if (data instanceof List<?> list) {
                    return Optional.of(list.stream()
                        .map(Object::toString)
                        .toList());
                } else if (data instanceof String string) {
                    return Optional.of(List.of(string.split(lineSeparator())));
                }
            }
        } catch (UnsupportedFlavorException | IOException e) {
            //ignore unknown flavors
        }
        return empty();
    }
}
