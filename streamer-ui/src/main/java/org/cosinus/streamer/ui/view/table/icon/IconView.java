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

package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.StreamerPanel;
import org.cosinus.streamer.ui.view.table.TableStreamerView;
import org.cosinus.swing.icon.IconSize;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ICON_SIZE;

public class IconView<T extends Streamable> extends TableStreamerView<T> {

    public static final String TREE_VIEW_NAME = "icon";

    @Autowired
    private Preferences iconPreferences;

    @Autowired
    private PreferencesProvider preferencesProvider;

    private IconCellEditor<T> iconCellEditor;

    public IconView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();
    }

    @Override
    public String getName() {
        return TREE_VIEW_NAME;
    }

    @Override
    protected IconTable<T> createDataTable() {
        return new IconTable<>();
    }

    @Override
    protected StreamerEditor<T> createStreamerEditor() {
        if (iconCellEditor == null) {
            iconCellEditor = new IconCellEditor<>(this);
            getContainer().add(iconCellEditor.getNameEditor());
        }
        return iconCellEditor;
    }

    /**
     * Show the icons at the next larger size allowed by the icon size preference, and save it.
     */
    @Override
    public void zoomIn() {
        IconSize current = getIconTable().getIconSize();
        getAvailableIconSizes()
            .stream()
            .filter(size -> size.getSize() > current.getSize())
            .findFirst()
            .ifPresent(this::saveIconSize);
    }

    /**
     * Show the icons at the next smaller size allowed by the icon size preference, and save it.
     */
    @Override
    public void zoomOut() {
        IconSize current = getIconTable().getIconSize();
        getAvailableIconSizes()
            .stream()
            .filter(size -> size.getSize() < current.getSize())
            .reduce((smaller, larger) -> larger)
            .ifPresent(this::saveIconSize);
    }

    /**
     * Show the icons at the default size of the icon size preference, and save it.
     */
    @Override
    public void zoomReset() {
        getDefaultPreferences()
            .flatMap(preferences -> preferences.findStringPreference(ICON_SIZE))
            .flatMap(IconSize::forText)
            .ifPresent(this::saveIconSize);
    }

    /**
     * @return the icon sizes allowed by the default icon size preference, from the smallest
     */
    private List<IconSize> getAvailableIconSizes() {
        return getDefaultPreferences()
            .flatMap(preferences -> preferences.findPreference(ICON_SIZE))
            .map(Preference::getValues)
            .map(values -> ((Collection<?>) values)
                .stream()
                .map(String::valueOf)
                .map(IconSize::forText)
                .flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(IconSize::getSize))
                .toList())
            .orElse(emptyList());
    }

    private Optional<Preferences> getDefaultPreferences() {
        return preferencesProvider.getDefaultPreferences();
    }

    /**
     * Save the icon size preference and show the icon views of both panels with the new size.
     */
    private void saveIconSize(IconSize iconSize) {
        if (iconSize == getIconTable().getIconSize()) {
            return;
        }
        iconPreferences.updatePreference(ICON_SIZE, iconSize.getText());
        try {
            preferencesProvider.savePreferences(iconPreferences);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        streamerViewHandler.getPanels()
            .stream()
            .map(StreamerPanel::getView)
            .filter(IconView.class::isInstance)
            .map(IconView.class::cast)
            .forEach(IconView::updateIconSize);
    }

    /**
     * Lay out the icons again with the icon size preference, keeping the current item visible.
     */
    private void updateIconSize() {
        int currentIndex = table.getCurrentIndex();
        table.onResize();
        if (currentIndex >= 0) {
            table.setCurrentIndex(currentIndex);
        }
        table.repaint();
    }

    private IconTable<T> getIconTable() {
        return (IconTable<T>) table;
    }

    public JTextPane resetCellEditor(JTextPane nameEditor) {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);
        int column = table.getTableModel().getColumnForIndex(index);

        return ((IconCellRenderer) table.getCellRenderer(row, column)).resetCellEditor(nameEditor, row, column);
    }
}
