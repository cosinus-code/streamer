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

/**
 * A host found in the network, accepting samba connections.
 *
 * @param address the ip address
 * @param name    the host name, or the ip address if it cannot be resolved
 */
public record SambaHost(String address, String name) {

    /**
     * Get the host name without the domain part, or the ip address if the name is not resolved.
     *
     * @return the short name
     */
    public String shortName() {
        return name.equals(address) || name.indexOf('.') < 0 ?
            name :
            name.substring(0, name.indexOf('.'));
    }

    /**
     * Check if a configured host refers to this host.
     *
     * @param host the configured host name or address
     * @return true if it matches the name or the address
     */
    public boolean matches(String host) {
        return host != null &&
            (host.equalsIgnoreCase(address) ||
                host.equalsIgnoreCase(name) ||
                host.equalsIgnoreCase(shortName()));
    }
}
