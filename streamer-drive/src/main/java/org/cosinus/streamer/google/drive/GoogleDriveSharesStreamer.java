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

package org.cosinus.streamer.google.drive;

import lombok.extern.slf4j.Slf4j;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.google.drive.model.GoogleDriveShare;
import org.cosinus.streamer.google.drive.model.GoogleDriveShareHandler;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Slf4j
public class GoogleDriveSharesStreamer implements ParentStreamer<GoogleDriveShareStreamer> {

    public static final String SHARES = "Shares";

    public static final String SHARED_FOLDER_URL_PATTERN =
        "https:/drive\\.google\\.com/drive/folders/([a-zA-Z0-9_-]+)";

    protected final GoogleDriveMainStreamer googleDriveMainStreamer;

    protected final GoogleDriveShareHandler googleDriveShareHandler;

    protected List<Value> details;

    public GoogleDriveSharesStreamer(final GoogleDriveMainStreamer googleDriveMainStreamer,
                                     final GoogleDriveShareHandler googleDriveShareHandler) {
        this.googleDriveMainStreamer = googleDriveMainStreamer;
        this.googleDriveShareHandler = googleDriveShareHandler;
    }

    @Override
    public Stream<GoogleDriveShareStreamer> stream() {
        return googleDriveShareHandler
            .getGoogleDriveShares()
            .map(this::createGoogleDriveShareStreamer);
    }

    protected GoogleDriveShareStreamer createGoogleDriveShareStreamer(final GoogleDriveShare googleDriveShare) {
        GoogleDriveShareStreamer googleDriveShareStreamer =
            new GoogleDriveShareStreamer(this, googleDriveShare);
        googleDriveShareStreamer.cacheFile();
        return googleDriveShareStreamer;
    }

    @Override
    public String getName() {
        return SHARES;
    }

    @Override
    public Path getPath() {
        return Paths.get(SHARES);
    }

    @Override
    public ParentStreamer<?> getParent() {
        return googleDriveMainStreamer;
    }

    @Override
    public GoogleDriveShareStreamer create(Path googleDriveSharePath, boolean parent) {
        return ofNullable(googleDriveSharePath)
            .filter(path -> path.startsWith(getPath()))
            .map(path -> path.subpath(1, path.getNameCount()))
            .map(Object::toString)
            .flatMap(this::sharedFolderId)
            .flatMap(sharedFolderId -> googleDriveMainStreamer.streamGoogleDriveUsers()
                .findFirst()
                .flatMap(googleDriveUserStreamer ->
                    ofNullable(googleDriveUserStreamer.findFileById(sharedFolderId))
                        .map(file -> new GoogleDriveShare(
                            file.getId(), file.getName(), googleDriveUserStreamer.getUserId()))
                        .map(googleDriveShare -> new GoogleDriveShareStreamer(this, googleDriveShare))
                ))
            .orElse(null);
    }

    public Optional<String> sharedFolderId(String url) {
        Pattern pattern = Pattern.compile(SHARED_FOLDER_URL_PATTERN);
        Matcher matcher = pattern.matcher(url);

        return matcher.find() ? ofNullable(matcher.group(1)) : empty();
    }

    @Override
    public List<Value> details() {
        init();
        return details;
    }

    @Override
    public void init() {
        if (details == null) {
            details = asList(
                new TextValue(getName()),
                new TextValue(""),
                new TextValue("")
            );
        }
    }
}
