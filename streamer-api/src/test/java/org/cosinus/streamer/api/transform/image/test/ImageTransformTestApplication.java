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

package org.cosinus.streamer.api.transform.image.test;

import org.cosinus.streamer.api.transform.image.ImageToImageBinaryStreamerTransformer;
import org.cosinus.streamer.api.transform.svg.SvgToImageBinaryStreamerTransformer;
import org.cosinus.swing.boot.SpringSwingBootApplication;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.springframework.context.annotation.Import;

/**
 * Test application loading the image to image and the svg to image transformers,
 * with the spring swing beans it needs (e.g. the image handler, auto-configured).
 * <p>
 * It is in its own package, so it doesn't scan the other test classes,
 * and the beans are imported explicitly.
 */
@SpringSwingBootApplication
@Import({ImageToImageBinaryStreamerTransformer.class, SvgToImageBinaryStreamerTransformer.class})
public class ImageTransformTestApplication extends SwingApplicationFrame {

    @Override
    protected void loadContent() {
    }
}
