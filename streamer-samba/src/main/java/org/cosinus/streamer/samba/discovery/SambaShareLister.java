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

package org.cosinus.streamer.samba.discovery;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.rapid7.client.dcerpc.mssrvs.ServerService;
import com.rapid7.client.dcerpc.mssrvs.dto.NetShareInfo1;
import com.rapid7.client.dcerpc.transport.RPCTransport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static com.rapid7.client.dcerpc.transport.SMBTransportFactories.SRVSVC;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Lists the shared folders of a samba server, using the server service (srvsvc) RPC.
 */
@Component
public class SambaShareLister {

    /**
     * Share type for disk shares (STYPE_DISKTREE); the high bits are flags (special, temporary).
     */
    private static final int SHARE_TYPE_DISK = 0;

    private static final int SHARE_TYPE_MASK = 0x0FFFFFFF;

    private static final int SHARE_FLAG_SPECIAL = 0x80000000;

    private static final String HIDDEN_SHARE_SUFFIX = "$";

    private static final int TIMEOUT_SECONDS = 10;

    /**
     * List the disk shares of a samba server, excluding the special and the hidden shares.
     *
     * @param host                  the server host
     * @param port                  the server port
     * @param authenticationContext the authentication to use
     * @return the names of the disk shares
     * @throws IOException if the connection or the share listing fails
     */
    public List<String> listDiskShares(String host,
                                       int port,
                                       AuthenticationContext authenticationContext) throws IOException {

        SmbConfig smbConfig = SmbConfig.builder()
            .withTimeout(TIMEOUT_SECONDS, SECONDS)
            .withSoTimeout(TIMEOUT_SECONDS, SECONDS)
            .build();

        try (SMBClient client = new SMBClient(smbConfig);
             Connection connection = client.connect(host, port)) {

            Session session = connection.authenticate(authenticationContext);
            RPCTransport transport = SRVSVC.getTransport(session);
            return new ServerService(transport)
                .getShares1()
                .stream()
                .filter(this::isVisibleDiskShare)
                .map(NetShareInfo1::getNetName)
                .toList();
        }
    }

    public boolean isVisibleDiskShare(NetShareInfo1 share) {
        return (share.getType() & SHARE_TYPE_MASK) == SHARE_TYPE_DISK &&
            (share.getType() & SHARE_FLAG_SPECIAL) == 0 &&
            share.getNetName() != null &&
            !share.getNetName().endsWith(HIDDEN_SHARE_SUFFIX);
    }
}
