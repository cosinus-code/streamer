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

package org.cosinus.streamer.api.transform;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.Streamer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TransformBinaryStreamerHandlerTest {

    private final TestBinaryStreamer source = new TestBinaryStreamer("data.csv", new byte[0]);

    @Test
    public void testWithoutTransformersReturnsTheSourceBinaryStreamer() {
        TransformBinaryStreamerHandler handler = handler();

        assertSame(source, handler.transformToBinaryStreamer(source, "xlsx"));
    }

    @Test
    public void testWithoutMatchingTransformerReturnsTheSourceBinaryStreamer() {
        FakeTransformer other = new FakeTransformer("xls");
        TransformBinaryStreamerHandler handler = handler(other);

        assertSame(source, handler.transformToBinaryStreamer(source, "xlsx"));
        assertEquals(List.of("xlsx"), other.checkedFormats);
        assertTrue(other.transformedFormats.isEmpty());
    }

    @Test
    public void testMatchingTransformerIsUsed() {
        FakeTransformer xlsx = new FakeTransformer("xlsx");
        TransformBinaryStreamerHandler handler = handler(xlsx);

        assertSame(xlsx.result, handler.transformToBinaryStreamer(source, "xlsx"));
        assertEquals(List.of(source), xlsx.transformedSources);
        assertEquals(List.of("xlsx"), xlsx.transformedFormats);
    }

    @Test
    public void testOnlyTheMatchingTransformerIsUsed() {
        FakeTransformer xls = new FakeTransformer("xls");
        FakeTransformer xlsx = new FakeTransformer("xlsx");
        FakeTransformer pdf = new FakeTransformer("pdf");
        TransformBinaryStreamerHandler handler = handler(xls, xlsx, pdf);

        assertSame(xlsx.result, handler.transformToBinaryStreamer(source, "xlsx"));
        assertTrue(xls.transformedFormats.isEmpty());
        assertTrue(pdf.transformedFormats.isEmpty());
    }

    @Test
    public void testOnlyOneTransformationWhenSeveralTransformersMatch() {
        FakeTransformer first = new FakeTransformer("xlsx");
        FakeTransformer second = new FakeTransformer("xlsx");
        TransformBinaryStreamerHandler handler = handler(first, second);

        BinaryStreamer result = handler.transformToBinaryStreamer(source, "xlsx");

        assertTrue(result == first.result || result == second.result);
        assertEquals(1, first.transformedFormats.size() + second.transformedFormats.size());
    }

    @Test
    public void testNullTargetFormatReturnsTheSourceBinaryStreamer() {
        TransformBinaryStreamerHandler handler = handler(new FakeTransformer("xlsx"));

        assertSame(source, handler.transformToBinaryStreamer(source, null));
    }

    private TransformBinaryStreamerHandler handler(BinaryStreamerTransformer... transformers) {
        return new TransformBinaryStreamerHandler(new LinkedHashSet<>(asList(transformers)));
    }

    /**
     * Transformer accepting a single target format, recording its calls.
     */
    private static class FakeTransformer implements BinaryStreamerTransformer {

        private final String targetFormat;

        private final BinaryStreamer result = new TestBinaryStreamer("result", new byte[0]);

        private final List<String> checkedFormats = new ArrayList<>();

        private final List<Streamer<?>> transformedSources = new ArrayList<>();

        private final List<String> transformedFormats = new ArrayList<>();

        private FakeTransformer(String targetFormat) {
            this.targetFormat = targetFormat;
        }

        @Override
        public BinaryStreamer transformStreamer(Streamer<?> sourceStreamer, String targetFormat) {
            transformedSources.add(sourceStreamer);
            transformedFormats.add(targetFormat);
            return result;
        }

        @Override
        public boolean canTransform(Streamer<?> sourceStreamer, String targetFormat) {
            checkedFormats.add(targetFormat);
            return this.targetFormat.equals(targetFormat);
        }
    }
}
