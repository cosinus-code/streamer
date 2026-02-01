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

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;
import lombok.Getter;
import org.cosinus.swing.security.Permissions;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static org.cosinus.streamer.google.drive.permissions.GoogleDrivePermissionType.ANYONE;
import static org.cosinus.streamer.google.drive.permissions.GoogleDrivePermissionType.findByType;
import static org.cosinus.streamer.google.drive.permissions.GoogleDriveRole.*;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

@Getter
public class GoogleDrivePermissions implements Permissions {

    public static final String ANYONE_WITH_LINK_ID ="anyoneWithLink";

    public static final String GOOGLE_DRIVE_OTHERS_ICON_NAME  = "google-drive-others";

    public static final GoogleDriveRole[] AVAILABLE_ROLES = new GoogleDriveRole[] {
        READER,
        COMMENTER,
        WRITER,
        RESTRICTED
    };

    @Autowired
    private Translator translator;

    private final Map<String, Permission> existingPermissionsMap;

    private final List<GoogleDriveUserPermission> userPermissions;

    @Getter
    private String link;

    public GoogleDrivePermissions(final File file) {
        injectContext(this);
        Map<String, User> ownersMap = file.getOwners()
            .stream()
            .collect(Collectors.toMap(User::getEmailAddress, identity()));
        existingPermissionsMap = file.getPermissions()
            .stream()
            .collect(Collectors.toMap(Permission::getId, identity()));

        this.userPermissions = file.getPermissions()
            .stream()
            .filter(permission -> !permission.getId().equals(ANYONE_WITH_LINK_ID))
            .map(permission -> GoogleDriveUserPermission.builder()
                .id(permission.getId())
                .displayName(permission.getDisplayName())
                .description(permission.getEmailAddress())
                .me(ofNullable(ownersMap.get(permission.getEmailAddress()))
                    .map(User::getMe)
                    .orElse(false))
                .type(findByType(permission.getType()))
                .role(findByRole(permission.getRole()))
                .iconName(permission.getPhotoLink())
                .build())
            .sorted()
            .collect(Collectors.toList());

        GoogleDriveUserPermission genericUser = ofNullable(existingPermissionsMap.get(ANYONE_WITH_LINK_ID))
            .map(permission -> GoogleDriveUserPermission.builder()
                .id(ANYONE_WITH_LINK_ID)
                .displayName(translator.translate("google-drive-others"))
                .description(translator.translate("google-drive-anyone-with-link"))
                .type(ANYONE)
                .role(findByRole(permission.getRole()))
                .iconName(GOOGLE_DRIVE_OTHERS_ICON_NAME)
                .build())
            .orElseGet(() -> GoogleDriveUserPermission.builder()
                .id(ANYONE_WITH_LINK_ID)
                .displayName(translator.translate("google-drive-others"))
                .description(translator.translate("google-drive-anyone-with-link"))
                .type(ANYONE)
                .role(RESTRICTED)
                .iconName(GOOGLE_DRIVE_OTHERS_ICON_NAME)
                .build());

        this.userPermissions.add(genericUser);
        this.link = file.getWebViewLink();
    }

    public Optional<Permission> getExistingPermission(final String permissionId) {
        return ofNullable(existingPermissionsMap.get(permissionId));
    }
}
