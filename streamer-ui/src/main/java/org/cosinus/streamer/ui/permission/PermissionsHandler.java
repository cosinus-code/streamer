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
import org.cosinus.swing.security.Permissions;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
public class PermissionsHandler {

    private final Map<String, PermissionsDialogHandler<?>> dialogHandlers;

    public PermissionsHandler(Set<PermissionsDialogHandler<?>> dialogHandlers) {
        this.dialogHandlers = dialogHandlers
            .stream()
            .collect(toMap(
                PermissionsDialogHandler::permissionsTypeHandled,
                identity()));
    }

    public <P extends Permissions> Optional<PermissionsDialogHandler<P>> findPermissionsDialogHandler(P permissions) {
        return ofNullable((PermissionsDialogHandler<P>) dialogHandlers.get(permissions.getType()));
    }
}
