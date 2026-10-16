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

package org.cosinus.streamer.samba.test;

import com.hierynomus.mssmb2.SMBApiException;
import org.cosinus.streamer.samba.connection.SambaConnectionFactory;
import org.cosinus.streamer.samba.discovery.SambaDiscovery;
import org.cosinus.streamer.samba.discovery.SambaHostScanner;
import org.cosinus.streamer.samba.discovery.SambaShareLister;
import org.cosinus.streamer.samba.model.SambaConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.hierynomus.mserref.NtStatus.STATUS_ACCOUNT_DISABLED;
import static com.hierynomus.mssmb2.SMB2MessageCommandCode.SMB2_SESSION_SETUP;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Samba discovery "finding" the hosts and the shares set by the test, instead of scanning the local networks.
 */
public class TestSambaDiscovery extends SambaDiscovery {

    private List<DiscoveredHost> discoveredHosts = List.of();

    private List<String> listedShares;

    private boolean guestDisabled;

    private final List<SambaConfiguration> listedConfigurations = new ArrayList<>();

    public TestSambaDiscovery(final SambaConnectionFactory sambaConnectionFactory) {
        super(new SambaHostScanner(), new SambaShareLister(), sambaConnectionFactory);
    }

    public void setDiscoveredHosts(List<DiscoveredHost> discoveredHosts) {
        this.discoveredHosts = discoveredHosts;
    }

    /**
     * @param listedShares the shares to be listed for any host, or null to fail the listing
     */
    public void setListedShares(List<String> listedShares) {
        this.listedShares = listedShares;
        guestDisabled = false;
        listedConfigurations.clear();
    }

    /**
     * @param guestDisabled true to deny the listing of the shares without credentials, as a disabled guest account
     */
    public void setGuestDisabled(boolean guestDisabled) {
        this.guestDisabled = guestDisabled;
    }

    /**
     * @return the host configurations whose shares were listed
     */
    public List<SambaConfiguration> getListedConfigurations() {
        return listedConfigurations;
    }

    @Override
    public List<String> listShares(SambaConfiguration configuration) throws IOException {
        listedConfigurations.add(configuration);
        if (guestDisabled && isBlank(configuration.username())) {
            throw new SMBApiException(STATUS_ACCOUNT_DISABLED.getValue(), SMB2_SESSION_SETUP,
                "Authentication failed for 'Guest'", null);
        }
        if (listedShares == null) {
            throw new IOException("Cannot list the shares of " + configuration.host());
        }
        return listedShares;
    }

    @Override
    public Map<String, SambaConfiguration> discover(Map<String, SambaConfiguration> existing) {
        return merge(existing, discoveredHosts);
    }
}
