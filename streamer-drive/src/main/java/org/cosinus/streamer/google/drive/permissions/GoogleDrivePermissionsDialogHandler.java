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

package org.cosinus.streamer.google.drive.permissions;

import org.cosinus.streamer.api.permissions.PermissionsDialogHandler;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.cosinus.swing.boot.SwingApplicationFrame.applicationFrame;

@Component
public class GoogleDrivePermissionsDialogHandler implements PermissionsDialogHandler<GoogleDrivePermissions> {

    private final Translator translator;

    private final DialogHandler dialogHandler;

    public GoogleDrivePermissionsDialogHandler(final Translator translator,
                                               final DialogHandler dialogHandler) {
        this.translator = translator;
        this.dialogHandler = dialogHandler;
    }

    @Override
    public Optional<GoogleDrivePermissions> showPermissionsDialog(GoogleDrivePermissions permissions) {
        return dialogHandler.showDialog(() -> new GoogleDrivePermissionsDialog(
                applicationFrame, translator.translate("google-drive-permissions-dialog-title"), permissions))
            .response();
    }

    @Override
    public String permissionsTypeHandled() {
        return GoogleDrivePermissions.class.toString();
    }
}
