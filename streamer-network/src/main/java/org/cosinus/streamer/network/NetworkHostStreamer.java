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


package org.cosinus.streamer.network;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;

import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK_SERVER;

/**
 * Streamer of a host shown under the "Network" root streamer.
 *
 * @param <S> the type of the streamed children: folders, or directly the remote files
 */
public interface NetworkHostStreamer<S extends Streamer<?>> extends ParentStreamer<S> {

    /**
     * @return the host configuration
     */
    NetworkHost getNetworkHost();

    default String getIconName() {
        return ICON_NETWORK_SERVER;
    }
}
