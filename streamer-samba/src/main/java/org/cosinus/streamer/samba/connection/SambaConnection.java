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

package org.cosinus.streamer.samba.connection;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.fileinformation.ShareInfo;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.remote.Connection;
import org.cosinus.streamer.samba.SambaMainStreamer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.hierynomus.msdtyp.AccessMask.DELETE;
import static com.hierynomus.msdtyp.AccessMask.GENERIC_READ;
import static com.hierynomus.msdtyp.AccessMask.GENERIC_WRITE;
import static com.hierynomus.mserref.NtStatus.STATUS_NOT_FOUND;
import static com.hierynomus.mserref.NtStatus.STATUS_OBJECT_NAME_NOT_FOUND;
import static com.hierynomus.mserref.NtStatus.STATUS_OBJECT_PATH_NOT_FOUND;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_CREATE;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OPEN_IF;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_OVERWRITE_IF;
import static com.hierynomus.mssmb2.SMB2ShareAccess.ALL;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Connection to a samba shared folder
 */
public class SambaConnection implements Connection<SambaFile> {

    private static final Logger LOG = LogManager.getLogger(SambaConnection.class);

    public static final String SEPARATOR = "/";

    private static final String SMB_SEPARATOR = "\\";

    private static final Set<String> SPECIAL_ENTRIES = Set.of(".", "..");

    @Autowired
    private SambaMainStreamer sambaMainStreamer;

    private final String name;

    private final SMBClient client;

    private final DiskShare diskShare;

    private final String remoteDir;

    public SambaConnection(final String name,
                           final SMBClient client,
                           final DiskShare diskShare,
                           final String remoteDir) {
        injectContext(this);
        this.name = name;
        this.client = client;
        this.diskShare = diskShare;
        this.remoteDir = sambaMainStreamer.normalize(remoteDir);
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public Stream<SambaFile> stream(String innerPath) {
        return diskShare
            .list(toSmbPath(innerPath))
            .stream()
            .filter(not(info -> SPECIAL_ENTRIES.contains(info.getFileName())))
            .map(info -> SambaFile.of(
                sambaMainStreamer.resolve(innerPath, info.getFileName()),
                info));
    }

    public Optional<SambaFile> info(String innerPath) {
        try {
            return Optional.of(SambaFile.of(
                sambaMainStreamer.normalize(innerPath),
                diskShare.getFileInformation(toSmbPath(innerPath))));
        } catch (SMBApiException ex) {
            if (isNotFound(ex)) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    public boolean exists(String innerPath) {
        return info(innerPath).isPresent();
    }

    @Override
    public InputStream inputStream(String innerPath) {
        File file = openFile(innerPath, EnumSet.of(GENERIC_READ), FILE_OPEN);
        return new FilterInputStream(file.getInputStream()) {
            @Override
            public void close() throws IOException {
                try (file) {
                    super.close();
                }
            }
        };
    }

    @Override
    public OutputStream outputStream(SambaFile remote, String innerPath, boolean append) {
        File file = openFile(innerPath,
            EnumSet.of(GENERIC_WRITE),
            append ? FILE_OPEN_IF : FILE_OVERWRITE_IF);
        return new FilterOutputStream(file.getOutputStream(append)) {
            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                out.write(bytes, offset, length);
            }

            @Override
            public void close() throws IOException {
                try (file) {
                    super.close();
                }
            }
        };
    }

    @Override
    public SambaFile save(SambaFile remoteToSave) {
        if (remoteToSave.directory()) {
            mkdirs(remoteToSave.innerPath());
        } else {
            close(openFile(remoteToSave.innerPath(), EnumSet.of(GENERIC_WRITE), FILE_CREATE));
        }
        return info(remoteToSave.innerPath()).orElse(null);
    }

    public void mkdirs(String innerPath) {
        String current = "";
        for (String segment : sambaMainStreamer.segments(innerPath).toList()) {
            current = sambaMainStreamer.resolve(current, segment);
            if (!diskShare.folderExists(toSmbPath(current))) {
                diskShare.mkdir(toSmbPath(current));
            }
        }
    }

    @Override
    public boolean delete(SambaFile remote, boolean moveToTrash) {
        if (moveToTrash) {
            throw new UnsupportedOperationException("Move to trash is not supported for samba");
        }
        if (remote.directory()) {
            diskShare.rmdir(toSmbPath(remote.innerPath()), true);
        } else {
            diskShare.rm(toSmbPath(remote.innerPath()));
        }
        return !exists(remote.innerPath());
    }

    public void rename(String innerPath, String newInnerPath) {
        try (DiskEntry entry = diskShare.open(
            toSmbPath(innerPath),
            EnumSet.of(DELETE, GENERIC_READ),
            null,
            ALL,
            FILE_OPEN,
            null)) {
            entry.rename(toSmbPath(newInnerPath), false);
        }
    }

    public ShareInfo shareInfo() {
        return diskShare.getShareInformation();
    }

    public boolean isConnected() {
        return diskShare.isConnected();
    }

    /**
     * Nothing to do on close, the connection is kept open in the pool.
     */
    @Override
    public void close() {
    }

    /**
     * Close the samba share and the underlying client connection.
     */
    public void disconnect() {
        try {
            diskShare.close();
        } catch (Exception ex) {
            LOG.warn("Failed to close samba share for connection {}", name, ex);
        } finally {
            client.close();
        }
    }

    protected File openFile(String innerPath, Set<AccessMask> accessMask, SMB2CreateDisposition createDisposition) {
        return diskShare.openFile(toSmbPath(innerPath), accessMask, null, ALL, createDisposition, null);
    }

    protected String toSmbPath(String innerPath) {
        return concat(sambaMainStreamer.segments(remoteDir), sambaMainStreamer.segments(innerPath))
            .collect(joining(SMB_SEPARATOR));
    }

    private boolean isNotFound(SMBApiException ex) {
        return ex.getStatus() == STATUS_OBJECT_NAME_NOT_FOUND ||
            ex.getStatus() == STATUS_OBJECT_PATH_NOT_FOUND ||
            ex.getStatus() == STATUS_NOT_FOUND;
    }

    private void close(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
