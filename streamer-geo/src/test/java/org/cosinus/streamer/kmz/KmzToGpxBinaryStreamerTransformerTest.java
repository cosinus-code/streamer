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

package org.cosinus.streamer.kmz;

import io.jenetics.jpx.GPX;
import io.jenetics.jpx.WayPoint;
import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.gpx.GpxStreamer;
import org.cosinus.swing.context.ApplicationContextInjector;
import org.cosinus.swing.translate.Translator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class KmzToGpxBinaryStreamerTransformerTest {

    private static final String KML = """
        <?xml version="1.0" encoding="UTF-8"?>
        <kml xmlns="http://www.opengis.net/kml/2.2">
          <Document>
            <Placemark>
              <LineString>
                <coordinates>23.59,46.77,350 23.60,46.78,360</coordinates>
              </LineString>
            </Placemark>
          </Document>
        </kml>
        """;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private final KmzToGpxBinaryStreamerTransformer transformer = new KmzToGpxBinaryStreamerTransformer();

    private AnnotationConfigApplicationContext context;

    private ApplicationContextInjector contextInjector;

    /**
     * The gpx streamers translate their detail names at creation, through the static application context,
     * so a minimal context with a translator returning the keys is enough.
     */
    @Before
    public void setUp() {
        context = new AnnotationConfigApplicationContext();
        context.registerBean(Translator.class, KeyTranslator::new);
        context.refresh();
        contextInjector = new ApplicationContextInjector(context);
    }

    @After
    public void tearDown() {
        contextInjector.destroy();
        context.close();
    }

    @Test
    public void testCanTransformKmzToGpx() {
        assertTrue(transformer.canTransform(new KmzStreamer(new FileBinaryStreamer(Path.of("track.kmz"))), "gpx"));
    }

    @Test
    public void testCannotTransformToOtherFormat() {
        assertFalse(transformer.canTransform(new KmzStreamer(new FileBinaryStreamer(Path.of("track.kmz"))), "kml"));
    }

    @Test
    public void testCannotTransformNonKmzStreamer() {
        assertFalse(transformer.canTransform(new GpxStreamer(new FileBinaryStreamer(Path.of("track.gpx"))), "gpx"));
        assertFalse(transformer.canTransform(new FileBinaryStreamer(Path.of("track.kmz")), "gpx"));
    }

    @Test
    public void testTransformKmzToGpx() throws IOException {
        Path kmz = kmzFile(KML);

        BinaryStreamer gpxStreamer = transformer.transformStreamer(new KmzStreamer(new FileBinaryStreamer(kmz)), "gpx");

        GPX gpx;
        try (InputStream input = gpxStreamer.inputStream()) {
            gpx = GPX.Reader.DEFAULT.read(input);
        }
        assertEquals(1, gpx.getTracks().size());
        List<WayPoint> points = gpx.getTracks().getFirst().getSegments().getFirst().getPoints();
        assertEquals(2, points.size());
        assertEquals(46.77, points.get(0).getLatitude().doubleValue(), 1e-9);
        assertEquals(23.59, points.get(0).getLongitude().doubleValue(), 1e-9);
        assertEquals(360, points.get(1).getElevation().orElseThrow().doubleValue(), 1e-9);
    }

    private Path kmzFile(String kml) throws IOException {
        Path kmz = temporaryFolder.newFile("track.kmz").toPath();
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(kmz))) {
            zip.putNextEntry(new ZipEntry("doc.kml"));
            zip.write(kml.getBytes(UTF_8));
            zip.closeEntry();
        }
        return kmz;
    }

    private static class KeyTranslator implements Translator {

        @Override
        public void init(Locale locale) {
        }

        @Override
        public String translate(String key, Object... parameters) {
            return key;
        }

        @Override
        public Optional<Locale> getLocale() {
            return Optional.empty();
        }

        @Override
        public Map<String, Locale> getAvailableLocales() {
            return Map.of();
        }
    }

    /**
     * {@link BinaryStreamer} on a local file, enough for the kmz streamer, which reads its file from the path.
     */
    private record FileBinaryStreamer(Path path) implements BinaryStreamer {

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public ParentStreamer<?> getParent() {
            return null;
        }

        @Override
        public InputStream inputStream() {
            try {
                return Files.exists(path) ? Files.newInputStream(path) : new ByteArrayInputStream(new byte[0]);
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }

        @Override
        public OutputStream outputStream(boolean append) {
            return new ByteArrayOutputStream();
        }
    }
}
