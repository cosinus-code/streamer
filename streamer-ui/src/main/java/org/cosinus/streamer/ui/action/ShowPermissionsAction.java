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
import org.cosinus.streamer.ui.model.StreamerPermissionsModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.ActionContext;
import org.cosinus.swing.action.ActionInContext;
import org.cosinus.swing.dialog.DialogHandler;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class ShowPermissionsAction implements ActionInContext {

    public static final String SHOW_STREAMER_PERMISSIONS_ACTION_ID = "show-streamer-permissions";

    private static final String STREAMER_PERMISSIONS_DIALOG = "streamerPermissionsDialog.json";

    private static final String ICON_PERMISSIONS = "permissions";

    private final StreamerViewHandler streamerViewHandler;

    private final DialogHandler dialogHandler;

    public ShowPermissionsAction(final StreamerViewHandler streamerViewHandler,
                                 final DialogHandler dialogHandler) {
        this.streamerViewHandler = streamerViewHandler;
        this.dialogHandler = dialogHandler;
    }

    @Override
    public void run(ActionContext context) {
        StreamerView<?, ?> streamerView = streamerViewHandler.getCurrentView();
        ofNullable(streamerView.getCurrentStreamer())
            .ifPresent(this::showStreamerPermissionsDialog);
    }

    private void showStreamerPermissionsDialog(Streamer<?> streamer) {
        StreamerPermissionsModel streamerPermissionsModel = new StreamerPermissionsModel(streamer);
        dialogHandler.showDialog(() -> dialogHandler
                .createDialog(applicationFrame, STREAMER_PERMISSIONS_DIALOG, streamerPermissionsModel))
            .response()
            .map(StreamerPermissionsModel::getPermissions)
            .ifPresent(streamer::setPermissions);
    }

    @Override
    public String getIconName() {
        return ICON_PERMISSIONS;
    }

    @Override
    public String getId() {
        return SHOW_STREAMER_PERMISSIONS_ACTION_ID;
    }
}
