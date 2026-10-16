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

import com.hierynomus.msfscc.fileinformation.ShareInfo;
import org.cosinus.stream.FlatStreamingSpliterator;
import org.cosinus.stream.FlatStreamingStrategy;
import org.cosinus.stream.StreamingStrategy;
import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.remote.RemoteParentStreamer;
import org.cosinus.streamer.samba.connection.SambaConnection;
import org.cosinus.streamer.samba.connection.SambaFile;
import org.cosinus.swing.error.AbortActionException;

import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Optional.ofNullable;

public class SambaParentStreamer extends SambaStreamer<SambaStreamer<?>>
    implements RemoteParentStreamer<SambaStreamer<?>, SambaFile, SambaConnection> {

    private ShareInfo shareInfo;

    public SambaParentStreamer(final Path path, final SambaFile sambaFile) {
        super(path, sambaFile);
    }

    /**
     * The remote files are collected eagerly, so the connection is returned to the pool right away,
     * without depending on the returned stream to be closed.
     */
    @Override
    public Stream<SambaStreamer<?>> stream() {
        return withConnection(connection -> connection
            .stream(getStreamQuery())
            .toList())
            .stream()
            .map(this::createFromRemote);
    }

    @Override
    public Stream<SambaStreamer<?>> flatStream(final FlatStreamingStrategy flatStreamingStrategy,
                                               final StreamingStrategy streamingStrategy,
                                               final StreamerFilter streamerFilter) {
        return StreamSupport.stream(
            new FlatStreamingSpliterator<>(
                flatStreamingStrategy,
                streamingStrategy,
                stream().filter(streamerFilter)), false);
    }

    @Override
    public SambaStreamer<?> createFromRemote(SambaFile sambaFile) {
        return sambaMainStreamer.createFromRemote(path.resolve(sambaFile.name()), sambaFile);
    }

    @Override
    public SambaStreamer<?> create(Path path, boolean parent) {
        //TODO: to find and use the corresponding sambaFile
        return sambaMainStreamer.createFromRemote(path, null);
    }

    @Override
    protected void createAndSave() {
        withConnection(connection -> {
            connection.mkdirs(getStreamQuery());
            return true;
        });
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public long getFreeSpace() {
        return ofNullable(getShareInfo())
            .map(ShareInfo::getFreeSpace)
            .orElse(0L);
    }

    @Override
    public long getTotalSpace() {
        return ofNullable(getShareInfo())
            .map(ShareInfo::getTotalSpace)
            .orElse(0L);
    }

    /**
     * @return the share information, or null if the user cancelled the credentials dialog,
     * as the free and total space are not worth an error
     */
    protected ShareInfo getShareInfo() {
        if (shareInfo == null) {
            try {
                shareInfo = withConnection(SambaConnection::shareInfo);
            } catch (AbortActionException ex) {
                return null;
            }
        }
        return shareInfo;
    }

    @Override
    public void reset() {
        super.reset();
        shareInfo = null;
    }
}
