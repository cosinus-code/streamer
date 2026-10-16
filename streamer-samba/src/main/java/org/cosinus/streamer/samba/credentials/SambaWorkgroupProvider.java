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


package org.cosinus.streamer.samba.credentials;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Provides the workgroup (or domain) of the current computer.
 */
@Component
public class SambaWorkgroupProvider {

    private static final Logger LOG = LogManager.getLogger(SambaWorkgroupProvider.class);

    /**
     * The environment variable with the domain of the logged user, on Windows.
     */
    private static final String USER_DOMAIN = "USERDOMAIN";

    private static final Path SAMBA_CONFIG = Paths.get("/etc/samba/smb.conf");

    private static final String GLOBAL_SECTION = "[global]";

    private static final String WORKGROUP_KEY = "workgroup";

    private final Map<String, String> environment;

    private final Path sambaConfig;

    @Autowired
    public SambaWorkgroupProvider() {
        this(System.getenv(), SAMBA_CONFIG);
    }

    public SambaWorkgroupProvider(final Map<String, String> environment, final Path sambaConfig) {
        this.environment = environment;
        this.sambaConfig = sambaConfig;
    }

    /**
     * Get the current workgroup: the domain of the logged user on Windows,
     * otherwise the workgroup from the global section of the samba configuration.
     *
     * @return the current workgroup, or empty if it is not found
     */
    public Optional<String> currentWorkgroup() {
        return ofNullable(environment.get(USER_DOMAIN))
            .filter(not(String::isBlank))
            .or(this::configuredWorkgroup);
    }

    /**
     * @return the workgroup from the global section of the samba configuration, or empty if not found
     */
    protected Optional<String> configuredWorkgroup() {
        if (!Files.isReadable(sambaConfig)) {
            return empty();
        }
        try {
            return configuredWorkgroup(Files.readAllLines(sambaConfig));
        } catch (IOException ex) {
            LOG.debug("Failed to read the samba configuration {}", sambaConfig, ex);
            return empty();
        }
    }

    protected Optional<String> configuredWorkgroup(List<String> lines) {
        boolean globalSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith(";")) {
                continue;
            }
            if (trimmed.startsWith("[")) {
                globalSection = trimmed.equalsIgnoreCase(GLOBAL_SECTION);
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (globalSection && separator > 0 &&
                trimmed.substring(0, separator).trim().equalsIgnoreCase(WORKGROUP_KEY)) {
                String workgroup = trimmed.substring(separator + 1).trim();
                return isBlank(workgroup) ? empty() : Optional.of(workgroup);
            }
        }
        return empty();
    }
}
