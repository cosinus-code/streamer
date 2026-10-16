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


package org.cosinus.streamer.samba.test;

import org.cosinus.streamer.samba.SambaMainStreamer;
import org.cosinus.streamer.samba.connection.SambaConnectionFactory;
import org.cosinus.streamer.samba.connection.SambaConnectionPool;
import org.cosinus.streamer.samba.connection.SambaConnectionPoolConfig;
import org.cosinus.streamer.samba.connection.TestNetworkConfigurationProvider;
import org.cosinus.streamer.samba.connection.TestDialogHandler;
import org.cosinus.streamer.samba.credentials.SambaWorkgroupProvider;
import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * Test application loading the samba main streamer with its beans,
 * with a test network configuration provider, dialog handler and discovery.
 * <p>
 * It is in its own package, so it doesn't scan the other test applications,
 * and the beans are imported explicitly.
 */
@SpringSwingBootApplication
@Import({
    SambaMainStreamer.class,
    SambaConnectionFactory.class,
    SambaConnectionPool.class,
    SambaConnectionPoolConfig.class,
    SambaWorkgroupProvider.class
})
public class SambaTestApplication extends SwingApplicationFrame {

    @Override
    protected void loadContent() {
    }

    @Bean
    public TestNetworkConfigurationProvider networkConfigurationProvider() {
        return new TestNetworkConfigurationProvider();
    }

    @Bean
    @Primary
    public TestDialogHandler testDialogHandler() {
        return new TestDialogHandler();
    }

    @Bean
    public TestSambaDiscovery sambaDiscovery(final SambaConnectionFactory sambaConnectionFactory) {
        return new TestSambaDiscovery(sambaConnectionFactory);
    }
}
