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

import lombok.Getter;
import org.cosinus.swing.file.api.FilePermissions;
import org.cosinus.swing.form.control.CheckBoxValue;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.UIModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StreamerPermissionsModel implements UIModel {

    private static final String OWNER_NAME = "owner-name";
    private static final String GROUP_NAME = "group-name";
    private static final String OWNER_PERMISSIONS = "owner-permissions";
    private static final String GROUP_PERMISSIONS = "group-permissions";
    private static final String OTHERS_PERMISSIONS = "others-permissions";
    private static final String SET_USER_ID = "set-user-id";
    private static final String SET_GROUP_ID = "set-group-id";
    private static final String STICKY = "sticky";
    private static final String TEXT_VIEW = "permissions-text-view";
    private static final String NUMBER_VIEW = "permissions-number-view";

    private static final String SHARED = "shared";
    private static final String SHARE_NAME = "share-name";
    private static final String SHARE_COMMENT = "share-comment";
    private static final String ALLOW_OTHERS_TO_EDIT = "allow-others-to-edit";
    private static final String GUEST_ACCESS = "guest-access";

    private static final String READ_PERMISSION_KEY = "read";
    private static final String WRITE_PERMISSION_KEY = "write";
    private static final String EXECUTE_PERMISSION_KEY = "execute";

    private static final Set<String> KEYS = Set.of(
        OWNER_NAME,
        GROUP_NAME,
        OWNER_PERMISSIONS,
        GROUP_PERMISSIONS,
        OTHERS_PERMISSIONS,
        SET_USER_ID,
        SET_GROUP_ID,
        STICKY,
        TEXT_VIEW,
        NUMBER_VIEW,
        SHARED,
        SHARE_NAME,
        SHARE_COMMENT,
        ALLOW_OTHERS_TO_EDIT,
        GUEST_ACCESS);

    @Autowired
    private Translator translator;

    private final String readPermissionName;

    private final String writePermissionName;

    private final String executePermissionName;

    @Getter
    private final FilePermissions permissions;

    public StreamerPermissionsModel(final FilePermissions permissions) {
        injectContext(this);
        this.readPermissionName = translator.translate(READ_PERMISSION_KEY);
        this.writePermissionName = translator.translate(WRITE_PERMISSION_KEY);
        this.executePermissionName = translator.translate(EXECUTE_PERMISSION_KEY);

        this.permissions = permissions;
    }

    @Override
    public Set<String> keys() {
        return KEYS;
    }

    @Override
    public void putValue(String key, Object value) {
        if (value != null) {
            switch (key) {
                case GROUP_NAME -> permissions.setGroupName(value.toString());
                case OWNER_PERMISSIONS -> {
                    List<CheckBoxValue> values = ((List<CheckBoxValue>) value);
                    permissions.setOwnerRead(values.get(0).selected());
                    permissions.setOwnerWrite(values.get(1).selected());
                    permissions.setOwnerExecute(values.get(2).selected());
                }
                case GROUP_PERMISSIONS -> {
                    List<CheckBoxValue> values = ((List<CheckBoxValue>) value);
                    permissions.setGroupRead(values.get(0).selected());
                    permissions.setGroupWrite(values.get(1).selected());
                    permissions.setGroupExecute(values.get(2).selected());
                }
                case OTHERS_PERMISSIONS -> {
                    List<CheckBoxValue> values = ((List<CheckBoxValue>) value);
                    permissions.setOthersRead(values.get(0).selected());
                    permissions.setOthersWrite(values.get(1).selected());
                    permissions.setOthersExecute(values.get(2).selected());
                }
                case SET_USER_ID -> permissions.setSetUserId(booleanValue(value));
                case SET_GROUP_ID -> permissions.setSetGroupId(booleanValue(value));
                case STICKY -> permissions.setSticky(booleanValue(value));
                case SHARED -> permissions.setShared(booleanValue(value));
                case SHARE_NAME -> permissions.setShareName(stringValue(value));
                case SHARE_COMMENT -> permissions.setShareComment(stringValue(value));
                case ALLOW_OTHERS_TO_EDIT -> permissions.setAllowOthersToEdit(booleanValue(value));
                case GUEST_ACCESS -> permissions.setGuestAccess(booleanValue(value));
            }
        }
    }

    @Override
    public Object getValue(String key) {
        return switch (key) {
            case OWNER_NAME -> permissions.getOwnerName();
            case GROUP_NAME -> permissions.getGroupName();
            case OWNER_PERMISSIONS -> asList(
                new CheckBoxValue(readPermissionName, permissions.isOwnerRead()),
                new CheckBoxValue(writePermissionName, permissions.isOwnerWrite()),
                new CheckBoxValue(executePermissionName, permissions.isOwnerExecute())
            );
            case GROUP_PERMISSIONS -> asList(
                new CheckBoxValue(readPermissionName, permissions.isGroupRead()),
                new CheckBoxValue(writePermissionName, permissions.isGroupWrite()),
                new CheckBoxValue(executePermissionName, permissions.isGroupExecute())
            );
            case OTHERS_PERMISSIONS -> asList(
                new CheckBoxValue(readPermissionName, permissions.isOthersRead()),
                new CheckBoxValue(writePermissionName, permissions.isOthersWrite()),
                new CheckBoxValue(executePermissionName, permissions.isOthersExecute())
            );
            case SET_USER_ID -> permissions.isSetUserId();
            case SET_GROUP_ID -> permissions.isSetGroupId();
            case STICKY -> permissions.isSticky();
            case TEXT_VIEW -> permissions.getTextView();
            case NUMBER_VIEW -> permissions.getNumberView();
            case SHARED -> permissions.isShared();
            case SHARE_NAME -> permissions.getShareName();
            case SHARE_COMMENT -> permissions.getShareComment();
            case ALLOW_OTHERS_TO_EDIT -> permissions.isAllowOthersToEdit();
            case GUEST_ACCESS -> permissions.isGuestAccess();
            default -> null;
        };
    }

    @Override
    public Object[] getValues(String key) {
        return permissions.getAvailableGroupNames();
    }

    @Override
    public boolean isReadonly(final String key) {
        return !permissions.isEditable();
    }
}
