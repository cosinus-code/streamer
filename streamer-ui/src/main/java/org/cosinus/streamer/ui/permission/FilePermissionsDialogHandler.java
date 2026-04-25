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

package org.cosinus.streamer.ui.permission;

import org.cosinus.streamer.api.permissions.PermissionsDialogHandler;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.file.api.FilePermissions;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class FilePermissionsDialogHandler implements PermissionsDialogHandler<FilePermissions> {

    private static final String STREAMER_PERMISSIONS_DIALOG = "streamerPermissionsDialog.json";

    private final DialogHandler dialogHandler;

    public FilePermissionsDialogHandler(final DialogHandler dialogHandler) {
        this.dialogHandler = dialogHandler;
    }

    @Override
    public Optional<FilePermissions> showPermissionsDialog(final FilePermissions permissions) {
        StreamerPermissionsModel streamerPermissionsModel = new StreamerPermissionsModel(permissions);
        return dialogHandler.showDialog(() -> dialogHandler
                .createDialog(applicationFrame, STREAMER_PERMISSIONS_DIALOG, streamerPermissionsModel))
            .response()
            .map(StreamerPermissionsModel::getPermissions);
    }

    @Override
    public String permissionsTypeHandled() {
        return FilePermissions.class.toString();
    }
}
