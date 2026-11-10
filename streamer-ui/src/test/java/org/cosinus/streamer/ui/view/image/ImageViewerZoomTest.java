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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.cosinus.streamer.ui.view.image.ImageViewer.MAX_ZOOM;
import static org.cosinus.streamer.ui.view.image.ImageViewer.MIN_ZOOM;
import static org.cosinus.streamer.ui.view.image.ImageViewer.ZOOM_STEP;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * The image viewer zooms the shown image in and out, starting from the scale it is shown at,
 * and the zoom reset fits the image in the viewer again.
 * <p>
 * The image is 400x200, left half red and right half blue, in a 200x200 viewer:
 * fitted, it is shown at half its size (200x100).
 */
public class ImageViewerZoomTest {

    private static final int VIEWER_SIZE = 200;

    private static final int IMAGE_WIDTH = 400;

    private static final int IMAGE_HEIGHT = 200;

    private static final double FIT_SCALE = 0.5;

    private static final double DELTA = 1e-9;

    private static final int RED = Color.RED.getRGB();

    private static final int BLUE = Color.BLUE.getRGB();

    private final Streamer<byte[]> imageStreamer = new TestImageStreamer("image.png");

    private ImageViewer imageViewer;

    @Before
    public void setUp() throws Exception {
        imageViewer = getOnEdt(this::createImageViewer);
    }

    @Test
    public void testZoomInStartsFromTheFitScale() throws Exception {
        showImage();
        onEdt(imageViewer::zoomIn);

        assertEquals(FIT_SCALE * ZOOM_STEP, imageViewer.getZoom(), DELTA);
        assertEquals(new Dimension(250, 125), imageViewer.getPreferredSize());
    }

    @Test
    public void testZoomOutStartsFromTheFitScale() throws Exception {
        showImage();
        onEdt(imageViewer::zoomOut);

        assertEquals(FIT_SCALE / ZOOM_STEP, imageViewer.getZoom(), DELTA);
        assertEquals(new Dimension(160, 80), imageViewer.getPreferredSize());
    }

    /**
     * Fitted, the image is 200 pixels wide, so the pixel at x = 110 is in its blue half;
     * zoomed in, it is 250 pixels wide, so the same pixel is in its red half.
     */
    @Test
    public void testZoomedImageIsPaintedAtTheZoomScale() throws Exception {
        showImage();
        assertEquals(BLUE, paint().getRGB(110, VIEWER_SIZE / 2));

        onEdt(imageViewer::zoomIn);
        assertEquals(RED, paint().getRGB(110, VIEWER_SIZE / 2));
    }

    @Test
    public void testZoomResetFitsTheImageAgain() throws Exception {
        showImage();
        BufferedImage fitted = paint();

        onEdt(() -> {
            imageViewer.zoomIn();
            imageViewer.zoomIn();
            imageViewer.zoomReset();
        });

        assertNull(imageViewer.getZoom());
        assertPaintedEquals(fitted, paint());
    }

    @Test
    public void testZoomIsLimited() throws Exception {
        showImage();
        onEdt(() -> {
            for (int i = 0; i < 50; i++) {
                imageViewer.zoomIn();
            }
        });
        assertEquals(MAX_ZOOM, imageViewer.getZoom(), DELTA);

        onEdt(() -> {
            for (int i = 0; i < 100; i++) {
                imageViewer.zoomOut();
            }
        });
        assertEquals(MIN_ZOOM, imageViewer.getZoom(), DELTA);
    }

    @Test
    public void testZoomDoesNothingWithoutImage() throws Exception {
        onEdt(() -> {
            imageViewer.reset(imageStreamer);
            imageViewer.finishLoading(imageViewer);
            imageViewer.zoomIn();
        });

        assertNull(imageViewer.getZoom());
    }

    @Test
    public void testNextImageIsFitted() throws Exception {
        showImage();
        BufferedImage fitted = paint();
        onEdt(imageViewer::zoomIn);

        showImage();

        assertNull(imageViewer.getZoom());
        assertPaintedEquals(fitted, paint());
    }

    private ImageViewer createImageViewer() {
        ImageViewer viewer = new ImageViewer();
        setField(viewer, "resourceResolver", new ClasspathResourceResolver(new PathMatchingResourcePatternResolver()));
        setField(viewer, "imageHandler", new ImageHandler(null));
        viewer.setSize(VIEWER_SIZE, VIEWER_SIZE);
        return viewer;
    }

    /**
     * Load the image in the viewer, as the image worker does: reset, update with the image, finish.
     */
    private void showImage() throws Exception {
        UpdatableImage image = createImage();
        onEdt(() -> {
            imageViewer.reset(imageStreamer);
            imageViewer.update(List.of(image));
            imageViewer.finishLoading(imageViewer);
        });
    }

    private UpdatableImage createImage() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, IMAGE_WIDTH / 2, IMAGE_HEIGHT);
        g.setColor(Color.BLUE);
        g.fillRect(IMAGE_WIDTH / 2, 0, IMAGE_WIDTH / 2, IMAGE_HEIGHT);
        g.dispose();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bytes);
        UpdatableImage image = new UpdatableImage("png");
        image.update(bytes.toByteArray());
        return image;
    }

    private BufferedImage paint() throws Exception {
        return getOnEdt(() -> {
            BufferedImage painted = new BufferedImage(VIEWER_SIZE, VIEWER_SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = painted.createGraphics();
            imageViewer.paint(g);
            g.dispose();
            return painted;
        });
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
