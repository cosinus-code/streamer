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

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;

import static org.junit.Assume.assumeTrue;

/**
 * A samba server running in a docker container, shared by the tests.
 * <p>
 * The container is started once and stopped by testcontainers at the end of the tests run.
 * The tests using it are skipped when docker is not available.
 */
public final class SambaTestServer {

    public static final String SHARE = "public";

    public static final String USERNAME = "tester";

    public static final String PASSWORD = "secret";

    public static final String HELLO_FILE = "sub/hello.txt";

    public static final String HELLO_CONTENT = "hello samba\n";

    private static final int SMB_PORT = 445;

    private static GenericContainer<?> sambaContainer;

    private SambaTestServer() {
    }

    public static synchronized void start() {
        assumeTrue("Docker is not available", DockerClientFactory.instance().isDockerAvailable());

        if (sambaContainer == null) {
            GenericContainer<?> container = new GenericContainer<>("dperson/samba:latest")
                .withExposedPorts(SMB_PORT)
                .withCopyToContainer(Transferable.of(HELLO_CONTENT), "/share/" + HELLO_FILE)
                .withCommand(
                    "-u", USERNAME + ";" + PASSWORD,
                    "-s", SHARE + ";/share;yes;no;no;" + USERNAME,
                    "-p")
                .waitingFor(Wait.forLogMessage(".*daemon 'smbd' finished starting up.*\\n", 1));
            container.start();
            sambaContainer = container;
        }
    }

    public static String host() {
        return sambaContainer.getHost();
    }

    public static int port() {
        return sambaContainer.getMappedPort(SMB_PORT);
    }
}
