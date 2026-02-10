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

package org.cosinus.streamer.ui.menu;

import org.cosinus.streamer.ui.action.FindAndLoadStreamerAction;
import org.cosinus.streamer.ui.favorites.FavoritesHandler;
import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.image.icon.IconInitializer;
import org.cosinus.swing.menu.MenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.isRightMouseButton;

@Component
public class MenuHandler {

    public static final String SEPARATOR = "separator";

    private final ActionController actionController;

    private final FavoritesHandler favoritesHandler;

    protected final IconInitializer iconInitializer;

    private PopupMenu favoritesPopup;

    public MenuHandler(final ActionController actionController,
                       final FavoritesHandler favoritesHandler,
                       final IconInitializer iconInitializer) {
        this.actionController = actionController;
        this.favoritesHandler = favoritesHandler;
        this.iconInitializer = iconInitializer;
    }

    public PopupMenu createPopupMenu(String... actionIds) {
        PopupMenu popupMenu = new PopupMenu();
        stream(actionIds)
            .forEach(actionId -> {
                if (SEPARATOR.equals(actionId)) {
                    popupMenu.addSeparator();
                } else {
                    popupMenu.add(createMenuItem(actionId));
                }
            });
        return popupMenu;
    }

    public MenuItem createMenuItem(String actionId) {
        return createMenuItem(actionId, null);
    }

    public MenuItem createMenuItem(String actionId, ActionModel actionModel) {
        ActionListener actionListener = ofNullable(actionModel)
            .<ActionListener>map(model -> e -> actionController.runAction(actionId, model))
            .orElse(e -> actionController.runAction(actionId));
        MenuItem menuItem = new MenuItem(actionListener, actionId);
        menuItem.translate();

        //TODO: this shouldn't be necessary, but sometimes the icon is not loaded by the IconInitializer
        iconInitializer.updateIcon(menuItem);
        return menuItem;
    }

    public void addContextMenu(final JComponent component, final PopupMenu popupContextMenu) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                if (isRightMouseButton(mouseEvent)) {
                    popupContextMenu.show(component,
                        mouseEvent.getX(),
                        mouseEvent.getY());
                }
            }
        });
    }

    public PopupMenu getFavoritesPopup() {
        if (favoritesPopup == null) {
            favoritesPopup = new PopupMenu();
            initFavoritesPopup();
        }
        return favoritesPopup;
    }

    public void initFavoritesPopup() {
        favoritesPopup.removeAll();
        favoritesHandler.getFavorites()
            .stream()
            .map(favorite -> new MenuItem(
                e -> new FindAndLoadStreamerAction(() -> favorite).run(),
                favorite))
            .forEach(favoritesPopup::add);
        favoritesPopup.translate();
    }
}
