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
package org.cosinus.streamer.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.network.NetworkHostStreamer;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.cosinus.streamer.ftp.FtpMainStreamer.FTP_NETWORK_TYPE;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FILE_SERVER;

/**
 * Streamer of an ftp host, streaming directly the remote files, as an ftp host has no folders level.
 */
public class FtpHostStreamer extends FtpParentStreamer implements NetworkHostStreamer<FtpStreamer<?>> {

    @Autowired
    private FtpMainStreamer ftpMainStreamer;

    public FtpHostStreamer(String connectionName) {
        super(new FTPFile(), Paths.get(connectionName), connectionName);
    }

    /**
     * @return the "Network" root streamer
     */
    @Override
    public ParentStreamer<?> getParent() {
        return ftpMainStreamer.getParent();
    }

    @Override
    public FtpConfiguration getNetworkHost() {
        return ftpMainStreamer.getFtpHostConfiguration(connectionName);
    }

    @Override
    public boolean exists() {
        return ftpMainStreamer.existHost(connectionName);
    }

    /**
     * Save a new ftp host in network.json.
     */
    @Override
    public void save() {
        if (!exists()) {
            ftpMainStreamer.saveHost(connectionName);
        }
    }

    /**
     * Rename the ftp host and save the new name in network.json.
     * <p>
     * Only a new name at the host level is accepted (a relative path with a single name).
     *
     * @param newPath the new path, with the new host name
     * @return true if the host was renamed
     */
    @Override
    public boolean rename(Path newPath) {
        return newPath != null &&
            !newPath.isAbsolute() &&
            newPath.getNameCount() == 1 &&
            ftpMainStreamer.renameHost(connectionName, newPath.toString());
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return ftpMainStreamer.deleteHost(connectionName);
    }

    @Override
    public boolean canUpdate() {
        return true;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    /**
     * @return the host name and address, as for the samba hosts
     */
    @Override
    public List<Value> details() {
        return List.of(
            new TextValue(getName()),
            new TextValue(getNetworkHost().host()),
            new TextValue(FTP_NETWORK_TYPE));
    }
}
