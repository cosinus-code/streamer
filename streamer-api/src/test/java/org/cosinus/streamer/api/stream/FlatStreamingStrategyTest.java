/*
 * Copyright 2025 Cosinus Software
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
package org.cosinus.streamer.api.stream;

import org.cosinus.streamer.api.StreamerFilter;
import org.cosinus.streamer.api.util.JsonStreamer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cosinus.stream.FlatStreamingStrategy.*;
import static org.cosinus.streamer.api.util.TestStreamerFactory.jsonStreamer;

/**
 * Tests for flat streaming strategies
 */
@RunWith(JUnit4.class)
public class FlatStreamingStrategyTest {
    /**
     * Test flat streaming using LEVEL_UP_BOTTOM strategy for traversing the streamers tree.
     *
     * @throws IOException while reading the file with json streamers
     */
    @Test
    public void testFlatStreamingStrategyLevelUpBottomOrder() throws IOException {
        //GIVEN
        JsonStreamer jsonStreamer = jsonStreamer("/flat-streaming/streamers.json");
        StreamerFilter filter = streamer -> true;

        //WHEN
        try (Stream<JsonStreamer> flatStream = jsonStreamer.flatStream(LEVEL_UP_BOTTOM, filter)) {
            List<String> flatNames = flatStream.map(JsonStreamer::getName).toList();

            //THEN
            assertThat(flatNames).containsExactly(
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17");
        }
    }

    /**
     * Test flat streaming using IN_DEPTH strategy for traversing the streamers tree.
     *
     * @throws IOException while reading the file with json streamers
     */
    @Test
    public void testFlatStreamingStrategyInDepth() throws IOException {
        //GIVEN
        JsonStreamer jsonStreamer = jsonStreamer("/flat-streaming/streamers.json");
        StreamerFilter filter = streamer -> true;

        //WHEN
        try (Stream<JsonStreamer> flatStream = jsonStreamer.flatStream(IN_DEPTH, filter)) {
            List<String> flatNames = flatStream.map(JsonStreamer::getName).toList();

            //THEN
            assertThat(flatNames).containsExactly(
                "1", "2", "5", "11", "12", "13", "3", "6", "7", "14", "4", "8", "15", "9", "10", "16", "17");
        }
    }

    /**
     * Test flat streaming using LEVEL_BOTTOM_UP strategy for traversing the streamers tree.
     *
     * @throws IOException while reading the file with json streamers
     */
    @Test
    public void testFlatStreamingStrategyLevelBottomUp() throws IOException {
        //GIVEN
        JsonStreamer jsonStreamer = jsonStreamer("/flat-streaming/streamers.json");
        StreamerFilter filter = streamer -> true;

        //WHEN
        try (Stream<JsonStreamer> flatStream = jsonStreamer.flatStream(LEVEL_BOTTOM_UP, filter)) {
            List<String> flatNames = flatStream.map(JsonStreamer::getName).toList();

            //THEN
            assertThat(flatNames).containsExactly(
                "11", "12", "13", "5", "2", "6", "14", "7", "3", "15", "8", "9", "17", "16", "10", "4", "1");
        }
    }
}
