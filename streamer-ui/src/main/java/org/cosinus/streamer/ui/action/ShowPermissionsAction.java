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
import org.cosinus.streamer.ui.permission.PermissionsHandler;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.security.Permissions;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.icon.IconProvider.ICON_PERMISSIONS;

@Component
public class ShowPermissionsAction implements SwingAction {

    public static final String SHOW_STREAMER_PERMISSIONS_ACTION_ID = "show-streamer-permissions";

    private static final String STREAMER_PERMISSIONS_DIALOG = "streamerPermissionsDialog.json";

    private final StreamerViewHandler streamerViewHandler;

    private final DialogHandler dialogHandler;

    private final Translator translator;

    private final PermissionsHandler permissionsHandler;

    public ShowPermissionsAction(final StreamerViewHandler streamerViewHandler,
                                 final DialogHandler dialogHandler,
                                 final Translator translator,
                                 final PermissionsHandler permissionsHandler) {
        this.streamerViewHandler = streamerViewHandler;
        this.dialogHandler = dialogHandler;
        this.translator = translator;
        this.permissionsHandler = permissionsHandler;
    }

    @Override
    public void run() {
        StreamerView<?, ?> streamerView = streamerViewHandler.getCurrentView();
        ofNullable(streamerView.getCurrentStreamer())
            .ifPresent(this::showStreamerPermissionsDialog);
    }

    private void showStreamerPermissionsDialog(Streamer<?> streamer) {
        Permissions permissions = streamer.getPermissions();
        if (permissions == null) {
            dialogHandler.showInfo(translator.translate("no-permissions-available"));
            return;
        }
        permissionsHandler
            .findPermissionsDialogHandler(permissions)
            .ifPresentOrElse(
                permissionsDialogHandler -> permissionsDialogHandler
                    .showPermissionsDialog(permissions)
                    .ifPresent(streamer::setPermissions),
                () -> dialogHandler.showInfo(translator.translate("unsupported-permissions-type"))
            );
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
