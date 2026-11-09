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

package org.cosinus.streamer.ui.view.image;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.UpdatableImage;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * The image viewer shows the broken photo icon when the image to show cannot be loaded,
 * also when moving to it from another image.
 * <p>
 * The viewer is painted in an image, on the event dispatch thread, and the painted pixels are compared.
 */
public class ImageViewerBrokenImageTest {

    private static final int VIEWER_SIZE = 200;

    private static final int IMAGE_COLOR = Color.RED.getRGB();

    private static final int BACKGROUND_COLOR = Color.BLACK.getRGB();

    private final Streamer<byte[]> shownImageStreamer = new TestImageStreamer("shown.png");

    private final Streamer<byte[]> brokenImageStreamer = new TestImageStreamer("broken.png");

    private ClasspathResourceResolver resourceResolver;

    private ImageViewer imageViewer;

    @Before
    public void setUp() throws Exception {
        resourceResolver = new ClasspathResourceResolver(new PathMatchingResourcePatternResolver());
        imageViewer = getOnEdt(this::createImageViewer);
    }

    @Test
    public void testBrokenPhotoIconIsAvailable() {
        assertTrue(resourceResolver.resolveImageAsBytes("broken-photo-icon.png").isPresent());
    }

    @Test
    public void testLoadedImageIsShown() throws Exception {
        showImage(imageViewer);

        assertEquals(IMAGE_COLOR, centerPixel(paint(imageViewer)));
    }

    @Test
    public void testBrokenPhotoIconIsShownWhenTheImageCannotBeLoaded() throws Exception {
        onEdt(() -> {
            imageViewer.reset(brokenImageStreamer);
            imageViewer.finishLoading(imageViewer);
        });

        BufferedImage painted = paint(imageViewer);
        assertNotEquals(IMAGE_COLOR, centerPixel(painted));
        assertPaintedEquals(paintBrokenImageViewer(), painted);
    }

    /**
     * When moving to the next image, the current image is cleared before the next one is loaded:
     * the background is shown while loading, not the previous image nor the broken photo icon.
     */
    @Test
    public void testCurrentImageIsClearedWhenTheNextImageStartsLoading() throws Exception {
        showImage(imageViewer);
        onEdt(() -> imageViewer.reset(brokenImageStreamer));

        BufferedImage painted = paint(imageViewer);
        assertEquals(BACKGROUND_COLOR, centerPixel(painted));
        assertFalse("the broken photo icon is shown while loading", Arrays.equals(
            paintBrokenImageViewer().getRGB(0, 0, VIEWER_SIZE, VIEWER_SIZE, null, 0, VIEWER_SIZE),
            painted.getRGB(0, 0, VIEWER_SIZE, VIEWER_SIZE, null, 0, VIEWER_SIZE)));
    }

    /**
     * When the next image cannot be loaded (no image is published for it),
     * the broken photo icon is shown instead of the previous image.
     */
    @Test
    public void testBrokenPhotoIconIsShownWhenMovingToAnImageWhichCannotBeLoaded() throws Exception {
        showImage(imageViewer);
        onEdt(() -> {
            imageViewer.reset(brokenImageStreamer);
            imageViewer.update(emptyList());
            imageViewer.finishLoading(imageViewer);
        });

        BufferedImage painted = paint(imageViewer);
        assertNotEquals(IMAGE_COLOR, centerPixel(painted));
        assertPaintedEquals(paintBrokenImageViewer(), painted);
    }

    /**
     * When moving back from a broken image to an image which can be loaded, the image is shown.
     */
    @Test
    public void testImageIsShownAfterABrokenImage() throws Exception {
        onEdt(() -> {
            imageViewer.reset(brokenImageStreamer);
            imageViewer.finishLoading(imageViewer);
        });
        showImage(imageViewer);

        assertEquals(IMAGE_COLOR, centerPixel(paint(imageViewer)));
    }

    private ImageViewer createImageViewer() {
        ImageViewer viewer = new ImageViewer();
        setField(viewer, "resourceResolver", resourceResolver);
        setField(viewer, "imageHandler", new ImageHandler(null));
        viewer.setSize(VIEWER_SIZE, VIEWER_SIZE);
        return viewer;
    }

    /**
     * Load an image in the viewer, as the image worker does: reset, update with the image, finish.
     */
    private void showImage(ImageViewer viewer) throws Exception {
        UpdatableImage image = createImage();
        onEdt(() -> {
            viewer.reset(shownImageStreamer);
            viewer.update(List.of(image));
            viewer.finishLoading(viewer);
        });
    }

    /**
     * @return the painting of a viewer which loaded only a broken image, so it shows the broken photo icon
     */
    private BufferedImage paintBrokenImageViewer() throws Exception {
        ImageViewer brokenImageViewer = getOnEdt(this::createImageViewer);
        onEdt(() -> {
            brokenImageViewer.reset(brokenImageStreamer);
            brokenImageViewer.finishLoading(brokenImageViewer);
        });
        return paint(brokenImageViewer);
    }

    private UpdatableImage createImage() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(VIEWER_SIZE, VIEWER_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setColor(new Color(IMAGE_COLOR));
        g.fillRect(0, 0, VIEWER_SIZE, VIEWER_SIZE);
        g.dispose();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bytes);
        UpdatableImage image = new UpdatableImage("png");
        image.update(bytes.toByteArray());
        return image;
    }

    private BufferedImage paint(ImageViewer viewer) throws Exception {
        return getOnEdt(() -> {
            BufferedImage painted = new BufferedImage(VIEWER_SIZE, VIEWER_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = painted.createGraphics();
            viewer.paint(g);
            g.dispose();
            return painted;
        });
    }

    private int centerPixel(BufferedImage painted) {
        return painted.getRGB(VIEWER_SIZE / 2, VIEWER_SIZE / 2);
    }

    private void assertPaintedEquals(BufferedImage expected, BufferedImage actual) {
        assertArrayEquals(
            expected.getRGB(0, 0, VIEWER_SIZE, VIEWER_SIZE, null, 0, VIEWER_SIZE),
            actual.getRGB(0, 0, VIEWER_SIZE, VIEWER_SIZE, null, 0, VIEWER_SIZE));
    }

    private void setField(Object target, String name, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void onEdt(Runnable runnable) throws Exception {
        SwingUtilities.invokeAndWait(runnable);
    }

    private <T> T getOnEdt(ThrowingSupplier<T> supplier) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                result.set(supplier.get());
            } catch (Exception ex) {
                error.set(ex);
            }
        });
        if (error.get() != null) {
            throw error.get();
        }
        return result.get();
    }

    /**
     * Image streamer which is only identified by its path, as the viewer only keeps the streamer it shows.
     */
    private record TestImageStreamer(String name) implements Streamer<byte[]> {

        @Override
        public Stream<byte[]> stream() {
            return Stream.empty();
        }

        @Override
        public ParentStreamer<?> getParent() {
            return null;
        }

        @Override
        public Path getPath() {
            return Path.of(name);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
