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

package org.cosinus.streamer.google.drive.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.google.drive.GoogleDriveComponent;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceLocator;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singleton;

@GoogleDriveComponent
public class GoogleDriveShareHandler extends JsonFileConverter<GoogleDriveShare> {

    public static final ResourceLocator GOOGLE_SHARE_RESOURCES = () -> "google/share";

    public static final String SHARED_LINK = "SharedLink";

    private final FilesystemResourceResolver resourceResolver;

    protected GoogleDriveShareHandler(final ObjectMapper objectMapper,
                                      final FilesystemResourceResolver resourceResolver) {
        super(objectMapper, GoogleDriveShare.class, singleton(resourceResolver));
        this.resourceResolver = resourceResolver;
    }

    public Stream<GoogleDriveShare> getGoogleDriveShares() {
        return resourceResolver
            .resolveResources(GOOGLE_SHARE_RESOURCES, "")
            .map(this::convert)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    @Override
    public Optional<GoogleDriveShare> convert(String name) {
        return convert(resourceResolver.getResourceSource(), name, this::toModel);
    }

    @Override
    protected String adjustName(String name) {
        return name + "/" + SHARED_LINK;
    }

    public void saveGoogleDriveShare(final GoogleDriveShare googleDriveShare) {
        try {
            super.saveModel(googleDriveShare.name(), googleDriveShare);
        } catch (IOException e) {
            throw new StreamerException("Failed to save Google Drive share: " + googleDriveShare.name(), e);
        }
    }

    public boolean deleteGoogleDriveShare(final GoogleDriveShare googleDriveShare) {
        try {
            return super.deleteFile(googleDriveShare.name());
        } catch (IOException e) {
            throw new StreamerException("Failed to delete Google Drive share: " + googleDriveShare.name(), e);
        }
    }

    @Override
    protected ResourceLocator resourceLocator() {
        return GOOGLE_SHARE_RESOURCES;
    }
}
