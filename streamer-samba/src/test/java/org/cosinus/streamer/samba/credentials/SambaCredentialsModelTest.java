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

package org.cosinus.streamer.samba.credentials;

import org.junit.Test;

import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_DOMAIN;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_LOCATION;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_PASSWORD;
import static org.cosinus.streamer.samba.credentials.SambaCredentialsModel.SAMBA_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SambaCredentialsModelTest {

    @Test
    public void testInitialValues() {
        SambaCredentialsModel model = new SambaCredentialsModel("\\\\nas\\public", "WORKGROUP", "user");

        assertEquals("\\\\nas\\public", model.getValue(SAMBA_LOCATION));
        assertEquals("WORKGROUP", model.getValue(SAMBA_DOMAIN));
        assertEquals("user", model.getValue(SAMBA_USERNAME));
        assertNull(model.getValue(SAMBA_PASSWORD));
    }

    @Test
    public void testUsernameDefaultsToLoggedUser() {
        assertEquals(System.getProperty("user.name"),
            new SambaCredentialsModel("\\\\nas\\public", null, null).getValue(SAMBA_USERNAME));
        assertEquals(System.getProperty("user.name"),
            new SambaCredentialsModel("\\\\nas\\public", null, " ").getValue(SAMBA_USERNAME));
    }

    @Test
    public void testBlankDomainBecomesNoDomain() {
        SambaCredentialsModel model = new SambaCredentialsModel("\\\\nas\\public", " ", "user");

        assertEquals(new SambaCredentials(null, "user", null), model.toCredentials());
    }

    @Test
    public void testEnteredValuesBecomeCredentials() {
        SambaCredentialsModel model = new SambaCredentialsModel("\\\\nas\\public", null, null);

        model.putValue(SAMBA_LOCATION, "ignored");
        model.putValue(SAMBA_DOMAIN, "OFFICE");
        model.putValue(SAMBA_USERNAME, "tester");
        model.putValue(SAMBA_PASSWORD, "secret");

        assertEquals("\\\\nas\\public", model.getValue(SAMBA_LOCATION));
        assertEquals(new SambaCredentials("OFFICE", "tester", "secret"), model.toCredentials());
    }
}
