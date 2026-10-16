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


package org.cosinus.streamer.network.context;

import org.cosinus.streamer.network.NetworkMainStreamer;
import org.cosinus.streamer.network.RootNetwork;
import org.cosinus.streamer.network.TestNetworkStreamer;
import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.springframework.context.annotation.Import;

/**
 * Test application with the "Network" root streamer and a network type found by the component scan,
 * as the {@link RootNetwork} annotated beans.
 */
@SpringSwingBootApplication
@Import(NetworkMainStreamer.class)
public class NetworkTestApplication extends SwingApplicationFrame {

    @Override
    protected void loadContent() {
    }

    @RootNetwork("Test")
    public static class ScannedNetworkStreamer extends TestNetworkStreamer {

        public ScannedNetworkStreamer() {
            super(null, "test://", "host");
        }
    }
}
