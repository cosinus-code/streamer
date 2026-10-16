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

import lombok.Getter;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.network.NetworkHostStreamer;
import org.cosinus.streamer.samba.model.SambaConfiguration;
import org.cosinus.streamer.samba.model.SambaFolderConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.api.file.BaseFileStreamer.DETAIL_INDEX_NAME;
import static org.cosinus.streamer.api.value.TranslatableName.translatableNames;
import static org.cosinus.streamer.samba.SambaMainStreamer.SAMBA_NETWORK_TYPE;
import static org.cosinus.streamer.samba.SambaMainStreamer.SAMBA_PROTOCOL;
import static org.cosinus.swing.image.icon.IconProvider.ICON_FILE_SERVER;

/**
 * Streamer of a samba server, containing the configured shared folders of that server.
 */
public class SambaHostStreamer implements NetworkHostStreamer<Streamer<?>> {

    private final SambaMainStreamer sambaMainStreamer;

    private final String hostName;

    @Getter
    private final SambaConfiguration hostConfiguration;

    private List<TranslatableName> detailNames;

    private List<Value> details;

    private volatile boolean foldersRefreshRequested;

    public SambaHostStreamer(final SambaMainStreamer sambaMainStreamer,
                             final String hostName,
                             final SambaConfiguration hostConfiguration) {
        this.sambaMainStreamer = sambaMainStreamer;
        this.hostName = hostName;
        this.hostConfiguration = hostConfiguration;
    }

    @Override
    public Stream<Streamer<?>> stream() {
        if (foldersRefreshRequested) {
            foldersRefreshRequested = false;
            sambaMainStreamer.refreshFolders(hostName);
        }

        return hostConfiguration.folders()
            .stream()
            .map(SambaFolderConfiguration::name)
            .map(shareName -> sambaMainStreamer.createRoot(getPath().resolve(shareName), true));
    }

    @Override
    public Streamer<?> create(Path path, boolean parent) {
        return sambaMainStreamer.create(path, parent);
    }

    @Override
    public Path getPath() {
        return Paths.get(hostName);
    }

    /**
     * @return the "Network" root streamer
     */
    @Override
    public ParentStreamer<?> getParent() {
        return sambaMainStreamer.getParent();
    }

    /**
     * The samba protocol, instead of the one of the "Network" root streamer,
     * so the url of the host is routed to the samba network type.
     */
    @Override
    public String getProtocol() {
        return SAMBA_PROTOCOL;
    }

    @Override
    public SambaConfiguration getNetworkHost() {
        return hostConfiguration;
    }

    @Override
    public boolean exists() {
        return sambaMainStreamer.existHost(hostName);
    }

    @Override
    public void save() {
        if (exists()) {
            ofNullable(details.get(DETAIL_INDEX_NAME))
                .map(Value::toString)
                .filter(not(getName()::equals))
                .map(Paths::get)
                .ifPresent(this::rename);
        } else {
            sambaMainStreamer.saveHost(this);
        }
    }

    /**
     * Rename the samba host and save the new name in network.json.
     * <p>
     * Only a new name at the host level is accepted (a relative path with a single name),
     * as a samba host cannot be moved elsewhere.
     *
     * @param newPath the new path, with the new host name
     * @return true if the host was renamed
     */
    @Override
    public boolean rename(Path newPath) {
        return newPath != null &&
            !newPath.isAbsolute() &&
            newPath.getNameCount() == 1 &&
            sambaMainStreamer.renameHost(hostName, newPath.toString());
    }

    @Override
    public String getDescription() {
        return hostConfiguration.description();
    }

    @Override
    public String getType() {
        return SAMBA_NETWORK_TYPE;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public boolean canDelete() {
        return true;
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return sambaMainStreamer.deleteHost(this);
    }

    /**
     * The shares are listed with the same details as the files.
     */
    @Override
    public List<TranslatableName> detailNames() {
        if (detailNames == null) {
            detailNames = translatableNames(
                DETAIL_KEY_NAME,
                DETAIL_KEY_TYPE,
                DETAIL_KEY_SIZE,
                DETAIL_KEY_TIME);
        }
        return detailNames;
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
                new TextValue(hostConfiguration.host()),
                new TextValue(SAMBA_NETWORK_TYPE));
        }
    }

    @Override
    public void reset() {
        details = null;
        foldersRefreshRequested = true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SambaHostStreamer that)) {
            return false;
        }
        return Objects.equals(hostName, that.hostName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName);
    }
}
