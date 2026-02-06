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

package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.compute.ComputeStreamerSizeExecutor;
import org.cosinus.streamer.ui.action.execute.compute.ComputeStreamerSizeModel;
import org.cosinus.streamer.ui.model.StreamerPropertiesModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.window.Dialog;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Optional;

import static java.awt.event.KeyEvent.VK_P;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;
import static org.cosinus.swing.image.icon.IconProvider.ICON_PROPERTIES;

@Component
public class ShowStreamerPropertiesAction implements ActionInContext {

    public static final String SHOW_STREAMER_PROPERTIES_ACTION_ID = "show-streamer-properties";

    private static final String STREAMER_PROPERTIES_DIALOG = "streamerPropertiesDialog.json";

    private final StreamerViewHandler streamerViewHandler;

    private final DialogHandler dialogHandler;

    private final ApplicationUIHandler uiHandler;

    private final ComputeStreamerSizeExecutor computeStreamerSizeExecutor;

    public ShowStreamerPropertiesAction(final StreamerViewHandler streamerViewHandler,
                                        final DialogHandler dialogHandler,
                                        final ApplicationUIHandler uiHandler,
                                        final ComputeStreamerSizeExecutor computeStreamerSizeExecutor) {
        this.streamerViewHandler = streamerViewHandler;
        this.dialogHandler = dialogHandler;
        this.uiHandler = uiHandler;
        this.computeStreamerSizeExecutor = computeStreamerSizeExecutor;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?, ?> streamerView = streamerViewHandler.getCurrentView();
        ofNullable(streamerView.getCurrentStreamer())
            .ifPresent(this::showStreamerPropertiesDialog);
    }

    private void showStreamerPropertiesDialog(Streamer<?> streamer) {
        StreamerPropertiesModel streamerPropertiesModel = new StreamerPropertiesModel(streamer);
        Dialog<StreamerPropertiesModel> dialog = dialogHandler
            .createDialog(applicationFrame, STREAMER_PROPERTIES_DIALOG, streamerPropertiesModel);

        dialogHandler.showDialog(() -> {
                if (streamer.getSize() < 0) {
                    ComputeStreamerSizeModel computeStreamerSizeModel = new ComputeStreamerSizeModel(streamer);
                    computeStreamerSizeModel.registerListeners(
                        streamerViewHandler.getCurrentView(),
                        streamerViewHandler.getOppositeView(),
                        dialog);
                    computeStreamerSizeExecutor.execute(computeStreamerSizeModel);
                }
                return dialog;
            })
            .response()
            .ifPresent(streamerProperties -> {
                ofNullable(streamerProperties.getNewName())
                    .filter(not(streamer.getName()::equals))
                    .flatMap(name -> ofNullable(streamer.getPath())
                        .map(Path::getParent)
                        .map(parent -> parent.resolve(name)))
                    .ifPresent(newName -> {
                        streamer.rename(newName);
                        streamerViewHandler.reloadViews();
                    });
                ofNullable(streamerProperties.getIconFile())
                    .ifPresent(iconFile -> {
                        //TODO
                    });
            });
    }

    @Override
    public String getId() {
        return SHOW_STREAMER_PROPERTIES_ACTION_ID;
    }

    @Override
    public String getIconName() {
        return ICON_PROPERTIES;
    }

    @Override
    public Optional<KeyStroke> getKeyStroke() {
        return Optional.of(uiHandler.getControlDownKeyStroke(VK_P));
    }
}
