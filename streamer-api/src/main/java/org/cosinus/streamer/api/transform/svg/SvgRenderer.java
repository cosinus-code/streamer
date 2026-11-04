/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.streamer.api.transform.svg;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import static java.lang.Float.parseFloat;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static java.util.Optional.ofNullable;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOWED_SCRIPT_TYPES;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_HEIGHT;
import static org.apache.batik.transcoder.SVGAbstractTranscoder.KEY_WIDTH;

/**
 * Renders svg content into an image, with batik.
 * <p>
 * The svg is rendered at its own size (given by its width and height, or else by its view box), multiplied by a scale,
 * with its transparency.
 * The scripts are not executed and the external resources (e.g. images or fonts referred by a file or an url)
 * are not loaded, so that the svg cannot read other files or urls.
 */
public final class SvgRenderer {

    /**
     * The largest image rendered for a scale greater than 1, to keep the memory used reasonable.
     */
    static final long MAX_SCALED_PIXELS = 40_000_000L;

    private SvgRenderer() {
    }

    /**
     * A rendered svg.
     *
     * @param image the image
     * @param scale the scale the svg was rendered at: the size of the svg is the size of the image divided by it
     */
    public record SvgImage(BufferedImage image, float scale) {

        public float svgWidth() {
            return image.getWidth() / scale;
        }

        public float svgHeight() {
            return image.getHeight() / scale;
        }
    }

    /**
     * @param svg   the svg content
     * @param scale the scale to render the svg at, reduced (down to 1) for a larger image than {@value #MAX_SCALED_PIXELS} pixels
     * @return the rendered svg
     * @throws IOException if the svg content is invalid
     */
    public static SvgImage render(final byte[] svg, final float scale) throws IOException {
        BufferedImageTranscoder transcoder = new BufferedImageTranscoder(scale);
        transcoder.addTranscodingHint(KEY_ALLOW_EXTERNAL_RESOURCES, false);
        transcoder.addTranscodingHint(KEY_EXECUTE_ONLOAD, false);
        transcoder.addTranscodingHint(KEY_ALLOWED_SCRIPT_TYPES, "");
        viewBoxSize(svg).ifPresent(size -> {
            transcoder.addTranscodingHint(KEY_WIDTH, size[0]);
            transcoder.addTranscodingHint(KEY_HEIGHT, size[1]);
        });
        try {
            transcoder.transcode(new TranscoderInput(new ByteArrayInputStream(svg)), null);
        } catch (TranscoderException | RuntimeException e) {
            throw new IOException("Invalid svg content: " + message(e), e);
        }
        return new SvgImage(transcoder.image, transcoder.appliedScale);
    }

    /**
     * Batik renders an svg without width and height at a default width (400) instead of the size of its view box.
     *
     * @return the size of the view box, if the root element has a view box and neither a width nor a height
     */
    static Optional<float[]> viewBoxSize(final byte[] svg) {
        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(svg));
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                        if (reader.getAttributeValue(null, "width") != null ||
                            reader.getAttributeValue(null, "height") != null) {
                            return Optional.empty();
                        }
                        return ofNullable(reader.getAttributeValue(null, "viewBox"))
                            .map(viewBox -> viewBox.strip().split("[\\s,]+"))
                            .filter(values -> values.length == 4)
                            .map(values -> new float[]{parseFloat(values[2]), parseFloat(values[3])})
                            .filter(size -> size[0] > 0 && size[1] > 0);
                    }
                }
            } finally {
                reader.close();
            }
        } catch (XMLStreamException | NumberFormatException e) {
            // the svg is rendered at the size given by batik, or fails when rendered
        }
        return Optional.empty();
    }

    private static String message(final Exception exception) {
        Throwable cause = exception instanceof TranscoderException transcoderException &&
            transcoderException.getException() != null ? transcoderException.getException() : exception;
        return cause.getMessage();
    }

    /**
     * Transcoder keeping the image rendered by batik.
     */
    private static class BufferedImageTranscoder extends ImageTranscoder {

        private final float scale;

        private float appliedScale = 1;

        private BufferedImage image;

        private BufferedImageTranscoder(final float scale) {
            this.scale = scale;
        }

        /**
         * The image size is the svg size multiplied by the scale, and batik scales the content to it.
         */
        @Override
        protected void setImageSize(final float documentWidth, final float documentHeight) {
            super.setImageSize(documentWidth, documentHeight);
            double pixels = (double) width * height;
            appliedScale = scale <= 1 ? scale : (float) Math.max(1, min(scale, sqrt(MAX_SCALED_PIXELS / pixels)));
            width *= appliedScale;
            height *= appliedScale;
        }

        @Override
        public BufferedImage createImage(final int width, final int height) {
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        @Override
        public void writeImage(final BufferedImage image, final TranscoderOutput output) {
            this.image = image;
        }
    }
}
