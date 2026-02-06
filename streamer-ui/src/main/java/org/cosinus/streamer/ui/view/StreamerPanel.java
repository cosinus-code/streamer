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

package org.cosinus.streamer.ui.view;

import org.cosinus.streamer.ui.favorites.FavoritesHandler;
import org.cosinus.streamer.ui.menu.MenuHandler;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.ProgressBar;
import org.cosinus.swing.form.control.NoBorderButton;
import org.cosinus.swing.form.control.TextField;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.image.icon.IconInitializer;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextFieldUI;
import java.awt.*;

import static java.awt.BorderLayout.*;
import static java.awt.Color.BLUE;
import static java.awt.Font.BOLD;
import static java.util.Optional.ofNullable;
import static org.cosinus.streamer.ui.action.FindAndLoadStreamerAction.findAndLoadStreamer;
import static org.cosinus.streamer.ui.preference.StreamerPreferences.ADDRESS_TOP;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.SystemColor.MENUITEM_FOREGROUND;
import static org.cosinus.swing.color.SystemColor.TEXT_PANE_SELECTION_BACKGROUND;

public class StreamerPanel extends Panel {

    public static final Color DEFAULT_FAVORITE_MARK_COLOR = BLUE;

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Autowired
    private Preferences preferences;

    @Autowired
    private Translator translator;

    @Autowired
    private FormatHandler formatHandler;

    @Autowired
    private FavoritesHandler favoritesHandler;

    @Autowired
    private MenuHandler menuHandler;

    @Autowired
    private IconInitializer iconInitializer;

    private final TextField addressTop;

    private final JLabel freeSpaceLabel;

    private final ProgressBar freeSpaceMarker;

    private NoBorderButton showViewsButton;

    private NoBorderButton addAsFavoriteButton;

    private NoBorderButton showFavoritesButton;

    private StreamerView<?, ?> view;

    public StreamerPanel() {
        this.addressTop = new TextField();
        this.freeSpaceMarker = new ProgressBar();
        this.freeSpaceLabel = new JLabel();

        setLayout(new BorderLayout());
    }

    @Override
    public void initComponents() {
        super.initComponents();

        freeSpaceLabel.setVisible(false);

        addressTop.addActionListener(event -> findAndLoadStreamer(addressTop::getText));

        showViewsButton = new NoBorderButton("⏷", this::showViewsSelector);
        addAsFavoriteButton = new NoBorderButton("★", this::addCurrentAddressAsFavorite);
        showFavoritesButton = new NoBorderButton("⏷", this::showFavoritesSelector);
        showFavoritesButton.setToolTipText(translator.translate("show-favorite-addresses"));

        Panel freesSpacePanel = new Panel(new BorderLayout(5, 5));
        freesSpacePanel.setBorder(emptyBorder(3, 5, 3, 3));
        freesSpacePanel.add(showViewsButton, WEST);
        freesSpacePanel.add(freeSpaceLabel, CENTER);
        freesSpacePanel.add(freeSpaceMarker, EAST);
        freesSpacePanel.setOpaque(false);

        Panel favoritesPanel = new Panel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        favoritesPanel.add(addAsFavoriteButton);
        favoritesPanel.add(showFavoritesButton);

        Panel addressTopPanel = new Panel(new BorderLayout());
        addressTopPanel.add(addressTop, CENTER);
        addressTopPanel.add(favoritesPanel, EAST);

        Panel topPanel = new Panel(new BorderLayout());
        topPanel.add(freesSpacePanel, NORTH);
        if (preferences.booleanPreference(ADDRESS_TOP)) {
            topPanel.add(addressTopPanel, SOUTH);
        }

        add(topPanel, NORTH);
    }

    @Override
    public void updateForm() {
        super.updateForm();

        freeSpaceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        addressTop.setUI(new BasicTextFieldUI());
        addressTop.setBorder(emptyBorder(3, 5, 3, 5));
        addressTop.setOpaque(false);
        addressTop.setFont(freeSpaceLabel.getFont().deriveFont(BOLD));
        addressTop.setForeground(freeSpaceLabel.getForeground());

        setAddress(addressTop.getText());
    }

    private void addCurrentAddressAsFavorite() {
        view.requestFocus();
        if (favoritesHandler.isFavorite(addressTop.getText())) {
            favoritesHandler.removeFavorite(addressTop.getText());
        } else {
            favoritesHandler.addFavorite(addressTop.getText());
        }
        menuHandler.initFavoritesPopup();
        updateFavoriteMark();
    }

    public void showFavoritesSelector() {
        view.requestFocus();
        ofNullable(menuHandler.getFavoritesPopup())
            .ifPresent(popup -> {
                popup.setVisible(true);
                popup.show(showFavoritesButton,
                    showFavoritesButton.getWidth() - popup.getWidth(),
                    showFavoritesButton.getHeight());
            });
    }

    public void showViewsSelector() {
        view.requestFocus();
        ofNullable(view.getAlternativeViewsPopup())
            .ifPresent(popup -> {
                popup.setVisible(true);
                popup.show(showViewsButton, 0, showFavoritesButton.getHeight() + 5);
            });
    }

    public void setView(StreamerView<?, ?> view) {
        if (this.view == view) {
            return;
        }

        ofNullable(this.view)
            .ifPresent(this::remove);
        add(view, CENTER);

        this.view = view;
        this.view.initComponents();
        this.view.triggerFormUpdate();

        ofNullable(view.getName())
            .flatMap(View::findByName)
            .map(View::getIconName)
            .ifPresent(showViewsButton::setIconName);
        iconInitializer.updateIcon(showViewsButton);

        revalidate();
    }

    public void setFreeSpace(long freeSpace, long totalSpace) {
        boolean isFreeSpaceRelevant = totalSpace > 0;
        freeSpaceMarker.setValue(isFreeSpaceRelevant ? (int) (100 - freeSpace * 100 / totalSpace) : 0);
        freeSpaceLabel.setText(isFreeSpaceRelevant ?
            translator.translate("free_memory",
                formatHandler.formatMemorySize(freeSpace),
                formatHandler.formatMemorySize(totalSpace)) : "");
        freeSpaceLabel.setVisible(isFreeSpaceRelevant);
        freeSpaceMarker.setVisible(isFreeSpaceRelevant);
    }

    public void setAddress(String address) {
        addressTop.setText(formatHandler.formatTextForLabel(addressTop, address));
        updateFavoriteMark();
    }

    private void updateFavoriteMark() {
        boolean isFavorite = favoritesHandler.isFavorite(addressTop.getText());
        addAsFavoriteButton.setForeground(isFavorite ?
            ofNullable(uiHandler.getColor(TEXT_PANE_SELECTION_BACKGROUND))
                .orElse(DEFAULT_FAVORITE_MARK_COLOR) :
            uiHandler.getColor(MENUITEM_FOREGROUND));
        addAsFavoriteButton.setToolTipText(translator.translate(isFavorite ?
            "remove-from-favorites" :
            "add-as-favorite"));
    }

    public StreamerView<?, ?> getView() {
        return view;
    }

    @Override
    public void translate() {
        ofNullable(view).ifPresent(StreamerView::translate);
    }

    @Override
    public void setEnabled(boolean enabled) {
        freeSpaceMarker.setEnabled(enabled);
        super.setEnabled(enabled);
    }
}
