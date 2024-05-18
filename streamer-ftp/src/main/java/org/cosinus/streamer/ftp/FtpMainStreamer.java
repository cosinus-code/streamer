/*
 * Copyright 2020 Cosinus Software
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

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.ftp.model.FtpConfiguration;
import org.cosinus.streamer.ftp.model.FtpConfigurationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK;

@RootStreamer("FTP")
@ConditionalOnProperty(name = "streamer.ftp.enabled", matchIfMissing = true)
public class FtpMainStreamer extends MainStreamer<FtpConnectionStreamer> {

    public static final String FTP_PROTOCOL = "ftp://";

    private final Map<String, FtpConfiguration> ftpConfigurationsMap;

    public FtpMainStreamer(final FtpConfigurationProvider ftpConfigurationProvider) {
        this.ftpConfigurationsMap = ftpConfigurationProvider.getConnectionModelsMap();
    }

    @Override
    public Stream<FtpConnectionStreamer> stream() {
        return ftpConfigurationsMap.keySet()
            .stream()
            .map(FtpConnectionStreamer::new);
    }

    @Override
    public String getProtocol() {
        return FTP_PROTOCOL;
    }

    @Override
    public Optional<Streamer<?>> findByPath(Path path) {
        String connectionName = ftpConfigurationsMap.keySet()
            .stream()
            .filter(getName()::equals)
            .findFirst()
            .orElse(null);

        return connectionName != null ?
            Optional.of(new FtpConnectionStreamer(connectionName)) :
            super.findByPath(path);
    }

    @Override
    public String getIconName() {
        return ICON_NETWORK;
    }
}
