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


package org.cosinus.streamer.ftp.credentials;

import org.junit.Test;

import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_LOCATION;
import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_PASSWORD;
import static org.cosinus.streamer.ftp.credentials.FtpCredentialsModel.FTP_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FtpCredentialsModelTest {

    @Test
    public void testInitialValues() {
        FtpCredentialsModel model = new FtpCredentialsModel("ftp://rebex", "demo");

        assertEquals("ftp://rebex", model.getValue(FTP_LOCATION));
        assertEquals("demo", model.getValue(FTP_USERNAME));
        assertNull(model.getValue(FTP_PASSWORD));
    }

    @Test
    public void testUsernameDefaultsToLoggedUser() {
        assertEquals(System.getProperty("user.name"),
            new FtpCredentialsModel("ftp://rebex", null).getValue(FTP_USERNAME));
    }

    @Test
    public void testEnteredValuesBecomeCredentials() {
        FtpCredentialsModel model = new FtpCredentialsModel("ftp://rebex", null);

        model.putValue(FTP_LOCATION, "ignored");
        model.putValue(FTP_USERNAME, "tester");
        model.putValue(FTP_PASSWORD, "secret");

        assertEquals("ftp://rebex", model.getValue(FTP_LOCATION));
        assertEquals(new FtpCredentials("tester", "secret"), model.toCredentials());
    }
}
