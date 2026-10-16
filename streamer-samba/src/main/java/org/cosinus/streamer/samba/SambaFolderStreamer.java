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

package org.cosinus.streamer.samba;

import org.cosinus.streamer.network.NetworkFolderStreamer;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.cosinus.streamer.samba.connection.SambaFile.sambaFolder;

/**
 * Streamer of a samba shared folder, corresponding to a share configured for a samba host.
 */
public class SambaFolderStreamer extends SambaParentStreamer implements NetworkFolderStreamer<SambaStreamer<?>> {

    private final SambaConfiguration hostConfiguration;

    private final SambaFolderConfiguration shareConfiguration;

    public SambaFolderStreamer(final String hostName,
                               final SambaConfiguration hostConfiguration,
                               final SambaFolderConfiguration shareConfiguration) {
        super(Paths.get(hostName, shareConfiguration.name()), sambaFolder(""));
        this.hostConfiguration = hostConfiguration;
        this.shareConfiguration = shareConfiguration;
    }

    @Override
    public SambaHostStreamer getParent() {
        return sambaMainStreamer.createHostStreamer(sambaMainStreamer.hostName(path));
    }

    @Override
    public SambaFolderConfiguration getNetworkFolder() {
        return shareConfiguration;
    }

    @Override
    public String getDescription() {
        return hostConfiguration.description(shareConfiguration.name());
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public void save() {
    }

    @Override
    public boolean rename(Path newPath) {
        return false;
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return false;
    }

    @Override
    public boolean canDelete() {
        return false;
    }
}
