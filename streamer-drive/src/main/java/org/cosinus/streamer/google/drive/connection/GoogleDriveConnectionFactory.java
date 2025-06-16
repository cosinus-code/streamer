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

package org.cosinus.streamer.google.drive.connection;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.model.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.AbstractConnectionFactory;
import org.cosinus.streamer.google.drive.GoogleDriveComponent;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceLocator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

import static com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.load;
import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static com.google.api.services.drive.DriveScopes.*;
import static java.util.Collections.singletonList;

@GoogleDriveComponent
public class GoogleDriveConnectionFactory extends AbstractConnectionFactory<String, GoogleDriveConnection, File> {

    private static final Logger LOG = LogManager.getLogger(GoogleDriveConnectionFactory.class);

    protected static final GsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();

    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static final String OFFLINE_ACCESS = "offline";

    private static final List<String> SCOPES = singletonList(DRIVE);

    public static final ResourceLocator GOOGLE_TOKENS_RESOURCES = () -> "google/tokens";

    private final FilesystemResourceResolver resourceResolver;

    public GoogleDriveConnectionFactory(final FilesystemResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Override
    public GoogleDriveConnection create(String userId) throws Exception {
        final NetHttpTransport httpTransport = newTrustedTransport();

        AuthorizationCodeInstalledApp googleAuthenticator = googleAuthenticator(httpTransport, userId);
        return new GoogleDriveConnection(googleAuthenticator, httpTransport, userId);
    }

    @Override
    public boolean validateConnection(GoogleDriveConnection connection) {
        return connection.areGoogleCredentialsValid();
    }

    @Override
    public void destroyConnection(GoogleDriveConnection connection) {
        LOG.info("Destroying google drive connection for user {}", connection.getUserId());
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param httpTransport The network HTTP Transport.
     * @param userId the user id
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private AuthorizationCodeInstalledApp googleAuthenticator(final NetHttpTransport httpTransport, String userId)
        throws IOException {

        try (InputStream input = GoogleDriveConnectionFactory.class.getResourceAsStream(CREDENTIALS_FILE_PATH)) {
            if (input == null) {
                throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
            }
            GoogleClientSecrets clientSecrets = load(GSON_FACTORY, new InputStreamReader(input));

            java.io.File googleTokensFolder = resourceResolver
                .resolveResourcePath(GOOGLE_TOKENS_RESOURCES, userId)
                .map(Path::toFile)
                .orElseThrow(() -> new FileNotFoundException("Cannot locate google tokens folder"));

            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(httpTransport, GSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(googleTokensFolder))
                .setScopes(SCOPES)
                .setAccessType(OFFLINE_ACCESS)
                .build();

            LocalServerReceiver receiver = new LocalServerReceiver
                .Builder()
                .setPort(8888)
                .build();
            return new AuthorizationCodeInstalledApp(flow, receiver);
        }
    }

}
