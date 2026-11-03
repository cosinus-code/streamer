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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.file.BaseFileStreamer;
import org.cosinus.streamer.api.remote.RemoteStreamer;
import org.cosinus.streamer.api.value.Value;
import org.cosinus.streamer.samba.connection.SambaConnection;
import org.cosinus.streamer.samba.connection.SambaConnectionFactory;
import org.cosinus.streamer.samba.connection.SambaConnectionPool;
import org.cosinus.streamer.samba.connection.SambaFile;
import org.cosinus.swing.error.AbortActionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Function;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.samba.SambaMainStreamer.SAMBA_PROTOCOL;

public abstract class SambaStreamer<T> extends BaseFileStreamer<T>
    implements RemoteStreamer<T, SambaFile, SambaConnection> {

    @Autowired
    protected SambaMainStreamer sambaMainStreamer;

    @Autowired
    protected SambaConnectionPool sambaConnectionPool;

    @Autowired
    protected SambaConnectionFactory sambaConnectionFactory;

    protected final Path path;

    protected SambaFile sambaFile;

    protected boolean remoteLoaded;

    /**
     * True if the user cancelled the credentials dialog when connecting for this streamer,
     * so the credentials are not asked again until the streamer is refreshed.
     */
    protected boolean credentialsCancelled;

    /**
     * @param path      the streamer path: host name, share name, then the path inside the share
     * @param sambaFile the remote file information, or null to be lazily loaded from the samba server
     */
    public SambaStreamer(final Path path, final SambaFile sambaFile) {
        this.path = path;
        this.sambaFile = sambaFile;
        this.remoteLoaded = sambaFile != null;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getProtocol() {
        return SAMBA_PROTOCOL;
    }

    /**
     * @return the connection key "host/share"
     */
    @Override
    public String connectionId() {
        return sambaConnectionFactory.connectionKey(
            sambaMainStreamer.hostName(path), sambaMainStreamer.shareName(path));
    }

    @Override
    public SambaConnectionPool connectionPool() {
        return sambaConnectionPool;
    }

    @Override
    public String getStreamQuery() {
        return sambaMainStreamer.toInnerPath(path);
    }

    @Override
    public SambaFile getRemote() {
        if (!remoteLoaded) {
            sambaFile = withConnection(connection -> connection.info(getStreamQuery()))
                .orElse(null);
            remoteLoaded = true;
        }
        return sambaFile;
    }

    protected void refreshRemote() {
        remoteLoaded = false;
        sambaFile = null;
    }

    /**
     * Run a function on a connection to the samba share of this streamer.
     * <p>
     * If the user cancelled the credentials dialog, the next connections of this streamer are aborted
     * without asking the credentials again (e.g. for the free space, after the content loading was aborted),
     * until the streamer is refreshed.
     *
     * @param function the function to run on the connection
     * @param <V>      the function result type
     * @return the function result
     * @throws AbortActionException if the user cancelled the credentials dialog
     */
    protected <V> V withConnection(Function<SambaConnection, V> function) {
        if (credentialsCancelled) {
            throw new AbortActionException("Samba credentials cancelled by user");
        }
        try {
            return sambaConnectionPool.withConnection(connectionId(), function);
        } catch (AbortActionException ex) {
            credentialsCancelled = true;
            throw ex;
        }
    }

    @Override
    public void reset() {
        super.reset();
        credentialsCancelled = false;
    }

    @Override
    public ParentStreamer<?> getParent() {
        return (ParentStreamer<?>) sambaMainStreamer.createRoot(path.getParent(), true);
    }

    @Override
    public boolean exists() {
        return getRemote() != null;
    }

    @Override
    public void save() {
        if (exists()) {
            ofNullable(details.get(DETAIL_INDEX_NAME))
                .map(Value::toString)
                .filter(not(getName()::equals))
                .ifPresent(this::rename);
        } else {
            createAndSave();
        }
        refreshRemote();
    }

    protected abstract void createAndSave();

    protected boolean rename(String newName) {
        return rename(path.resolveSibling(newName));
    }

    @Override
    public boolean rename(Path newPath) {
        if (newPath.getNameCount() <= 2 ||
            !connectionId().equals(sambaConnectionFactory.connectionKey(
                sambaMainStreamer.hostName(newPath), sambaMainStreamer.shareName(newPath)))) {
            return false;
        }

        String query = getStreamQuery();
        String newQuery = sambaMainStreamer.toInnerPath(newPath);

        // samba shares are usually case-insensitive, so allow renaming to a different case only
        if (!newQuery.equalsIgnoreCase(query) && withConnection(connection -> connection.exists(newQuery))) {
            throw new StreamerException("rename.error.already.exist", newPath);
        }
        withConnection(connection -> {
            connection.rename(query, newQuery);
            return true;
        });
        return true;
    }

    @Override
    public boolean delete(boolean moveToTrash) {
        return ofNullable(getRemote())
            .map(remote -> withConnection(connection -> connection.delete(remote, moveToTrash)))
            .orElse(false);
    }

    @Override
    public boolean isHidden() {
        return ofNullable(getRemote())
            .map(SambaFile::hidden)
            .orElse(false);
    }

    @Override
    public boolean canUpdate() {
        return ofNullable(getRemote())
            .map(remote -> !remote.readOnly())
            .orElse(true);
    }

    @Override
    public long getSize() {
        return ofNullable(getRemote())
            .map(SambaFile::size)
            .orElse(0L);
    }

    @Override
    public long lastModified() {
        return ofNullable(getRemote())
            .map(SambaFile::lastModified)
            .orElse(0L);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SambaStreamer<?> that)) {
            return false;
        }

        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
