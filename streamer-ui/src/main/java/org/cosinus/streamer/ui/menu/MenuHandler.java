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
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.menu.MenuItem;
import org.cosinus.swing.menu.PopupMenu;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.awt.event.MouseEvent.BUTTON3;
import static java.util.Arrays.stream;

@Component
public class MenuHandler {

    public static final String SEPARATOR = "separator";

    private final ActionController actionController;

    private final FavoritesHandler favoritesHandler;

    private PopupMenu favoritesPopup;

    public MenuHandler(final ActionController actionController,
                       final FavoritesHandler favoritesHandler, IconHandler iconHandler) {
        this.actionController = actionController;
        this.favoritesHandler = favoritesHandler;
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

    public void addMenuItems(PopupMenu popupMenu, String... actionIds) {
        stream(actionIds)
            .map(this::createMenuItem)
            .forEach(popupMenu::add);
    }

    public MenuItem createMenuItem(String actionId) {
        MenuItem menuItem = new MenuItem(e -> actionController.runAction(actionId), actionId);
        menuItem.translate();
        return menuItem;
    }

    public void addContextMenu(final JComponent component, final PopupMenu popupContextMenu) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == BUTTON3) {
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
