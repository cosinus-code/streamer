/*
 * Copyright 2025 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.streamer.google.drive;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.google.drive.connection.GoogleDriveUsersProvider;
import org.cosinus.streamer.google.drive.model.GoogleDriveShareHandler;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.cosinus.swing.image.icon.IconProvider.ICON_GOOGLE_DRIVE;

@RootStreamer("Google Drive")
@GoogleDriveComponent
public class GoogleDriveMainStreamer extends MainStreamer<ParentStreamer<? extends GoogleDriveStreamer<?>>> {

    public static final String DRIVE_PROTOCOL = "drive://";

    private final List<String> googleDriveUserIds;

    private final GoogleDriveSharesStreamer googleDriveSharesStreamer;

    private List<TranslatableName> detailNames;

    public GoogleDriveMainStreamer(final GoogleDriveUsersProvider googleDriveUsersProvider,
                                   final GoogleDriveShareHandler shareProvider) {
        this.googleDriveUserIds = googleDriveUsersProvider.getGoogleDriveUserIds();
        this.googleDriveSharesStreamer = new GoogleDriveSharesStreamer(this, shareProvider);
    }

    @Override
    public Stream<ParentStreamer<? extends GoogleDriveStreamer<?>>> stream() {
        return concat(
            streamGoogleDriveUsers(),
            Stream.of(googleDriveSharesStreamer)
        );
    }

    public Stream<GoogleDriveUserStreamer> streamGoogleDriveUsers() {
        return googleDriveUserIds
            .stream()
            .map(GoogleDriveUserStreamer::new);
    }

    @Override
    public String getIconName() {
        return ICON_GOOGLE_DRIVE;
    }

    @Override
    public String getProtocol() {
        return DRIVE_PROTOCOL;
    }

    @Override
    public Optional<Streamer<?>> findByPath(Path path) {
        return googleDriveUserIds
            .stream()
            .filter(path::startsWith)
            .findFirst()
            .map(GoogleDriveUserStreamer::new)
            .map(googleDriveUserStreamer -> path.getNameCount() > 1 ?
                googleDriveUserStreamer
                    .getFromRemote(connection -> connection.findFileByPath(path))
                    .map(file -> googleDriveUserStreamer.createFromRemoteFileWithPath(file, path))
                    .orElse(null) :
                googleDriveUserStreamer);
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }

    @Override
    public void init() {
        detailNames = TranslatableName.translatableNames(
            DETAIL_KEY_NAME,
            DETAIL_KEY_FREE_MEMORY,
            DETAIL_KEY_TOTAL_MEMORY
        );
    }
}
