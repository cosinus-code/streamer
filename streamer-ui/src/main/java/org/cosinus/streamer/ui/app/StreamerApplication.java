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

/*
 * MainApp.java
 *
 */

package org.cosinus.streamer.ui.app;

import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;

import static org.cosinus.swing.boot.SpringSwingApplication.run;

@SpringSwingBootApplication(scanBasePackages = "org.cosinus.streamer")
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
@EnableCaching
public class StreamerApplication {

    public static void main(String[] args) {
        run(StreamerApplication.class, args);
    }
}
