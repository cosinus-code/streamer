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

import lombok.Getter;

import static java.util.Arrays.stream;

public enum GoogleDriveRole {

    OWNER("owner", "Owner"),
    ORGANIZER("organizer", "Organizer"),
    FILE_ORGANIZER("fileOrganizer", "File Organizer"),
    WRITER("writer", "Editor"),
    COMMENTER("commenter", "Commenter"),
    READER("reader", "Viewer"),
    RESTRICTED("restricted", "Restricted");

    @Getter
    private final String key;

    @Getter
    private final String name;

    GoogleDriveRole(final String key, final String name) {
        this.key = key;
        this.name = name;
    }

    public static GoogleDriveRole findByRole(String key) {
        return stream(values())
            .filter(role -> role.getKey().equalsIgnoreCase(key))
            .findFirst()
            .orElse(null);
    }

    @Override
    public String toString() {
        return getName();
    }
}
