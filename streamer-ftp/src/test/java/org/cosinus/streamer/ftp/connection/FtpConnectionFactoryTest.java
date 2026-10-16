/*
 * Copyright 2026 Cosinus Software
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


package org.cosinus.streamer.ftp.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.streamer.ftp.credentials.FtpCredentials;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.network.NetworkConfigurationProvider;
import org.cosinus.streamer.network.NetworkHost;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.AbortActionException;
import org.cosinus.swing.ui.UIModel;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;

import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_LOCATION;
import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_PASSWORD;
import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Tests asking the ftp credentials, with an ftp server accepting only "demo" / "secret".
 */
public class FtpConnectionFactoryTest {

    private static final String HOST = "rebex";

    private FtpConfiguration hostConfiguration;

    private final Queue<FtpCredentials> enteredCredentials = new LinkedList<>();

    private final List<String> askedLocations = new ArrayList<>();

    private final List<String> loggedUsernames = new ArrayList<>();

    private FtpConnectionFactory ftpConnectionFactory;

    @Before
    public void createFactory() {
        // the host configuration set by each test, instead of reading network.json
        NetworkConfigurationProvider provider = new NetworkConfigurationProvider(new ObjectMapper(), Set.of()) {
            @Override
            public Map<String, NetworkHost> getConnectionModelsMap() {
                return Map.of(HOST, hostConfiguration);
            }
        };
        // the credentials dialog answered with the credentials set by each test, instead of being shown
        DialogHandler dialogHandler = new DialogHandler(null, null, null) {
            @Override
            public <M extends UIModel> Optional<M> lockAndShowDialog(Frame parent,
                                                                     String descriptorName,
                                                                     Supplier<Optional<M>> modelSupplier) {
                return modelSupplier.get()
                    .flatMap(model -> {
                        askedLocations.add((String) model.getValue(FTP_LOCATION));
                        // an empty queue answers as cancelled
                        return Optional.ofNullable(enteredCredentials.poll())
                            .map(credentials -> {
                                model.putValue(FTP_USERNAME, credentials.username());
                                model.putValue(FTP_PASSWORD, credentials.password());
                                return model;
                            });
                    });
            }
        };
        ftpConnectionFactory = new FtpConnectionFactory(provider, dialogHandler) {
            @Override
            protected FtpConnection connect(String name, FtpConfiguration ftpConfiguration) {
                return new FtpConnection(name);
            }

            @Override
            protected boolean login(FtpConnection ftpConnection, FtpConfiguration ftpConfiguration) {
                loggedUsernames.add(ftpConfiguration.username());
                return "demo".equals(ftpConfiguration.username()) && "secret".equals(ftpConfiguration.password());
            }

            @Override
            public void destroyConnection(FtpConnection ftpConnection) {
            }
        };
    }

    @Test
    public void testConfiguredCredentialsAreNotAsked() throws Exception {
        hostConfiguration = host("demo", "secret");

        assertNotNull(ftpConnectionFactory.create(HOST));
        assertTrue(askedLocations.isEmpty());
    }

    @Test
    public void testMissingCredentialsAreAsked() throws Exception {
        hostConfiguration = host("demo", null);
        enteredCredentials.add(new FtpCredentials("demo", "secret"));

        assertNotNull(ftpConnectionFactory.create(HOST));
        assertEquals(List.of("ftp://test.rebex.net"), askedLocations);
        assertEquals(List.of("demo"), loggedUsernames);
    }

    @Test
    public void testEnteredCredentialsAreReused() throws Exception {
        hostConfiguration = host(null, null);
        enteredCredentials.add(new FtpCredentials("demo", "secret"));

        assertNotNull(ftpConnectionFactory.create(HOST));
        assertNotNull(ftpConnectionFactory.create(HOST));
        assertEquals(1, askedLocations.size());
    }

    @Test
    public void testRefusedEnteredCredentialsAreAskedAgain() throws Exception {
        hostConfiguration = host(null, null);
        enteredCredentials.add(new FtpCredentials("demo", "wrong"));
        enteredCredentials.add(new FtpCredentials("demo", "secret"));

        assertNotNull(ftpConnectionFactory.create(HOST));
        assertEquals(2, askedLocations.size());
        assertEquals(List.of("demo", "demo"), loggedUsernames);
    }

    @Test
    public void testCancelledCredentialsAbortTheConnection() {
        hostConfiguration = host(null, null);

        assertThrows(AbortActionException.class, () -> ftpConnectionFactory.create(HOST));
        assertEquals(1, askedLocations.size());
        assertTrue(loggedUsernames.isEmpty());
    }

    @Test
    public void testRefusedConfiguredCredentialsAreNotAsked() throws Exception {
        hostConfiguration = host("demo", "wrong");

        assertNull(ftpConnectionFactory.create(HOST));
        assertTrue(askedLocations.isEmpty());
    }

    private FtpConfiguration host(String username, String password) {
        return new FtpConfiguration(HOST, "test.rebex.net", 0, username, password, 0, false,
            null, null, null, 0, null);
    }
}
