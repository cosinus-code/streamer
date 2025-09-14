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

package org.cosinus.streamer.strava.statististics;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.value.TranslatableName;
import org.cosinus.streamer.strava.model.AthleteStatistics;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.cosinus.streamer.api.ParentStreamer.FOLDER_VIEW_ID;
import static org.cosinus.streamer.strava.statististics.StravaStatisticsJsonStreamer.STATISTICS_ICON_NAME;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StravaStatisticsStreamer extends ExpandedStreamer<StravaStatisticStreamer> {

    public static final String DETAIL_KEY_COUNT = "count";

    public static final String DETAIL_KEY_DISTANCE = "distance";

    public static final String DETAIL_KEY_MOVING_TIME = "moving-time";

    public static final String DETAIL_KEY_ELAPSED_TIME = "elapsed-time";

    public static final String DETAIL_KEY_ELEVATION_GAIN = "elevation-gain";

    public static final String DETAIL_KEY_ACHIEVEMENT_COUNT = "achievement-count";

    public static final String DETAIL_KEY_BIGGEST_DISTANCE = "biggest-distance";

    public static final String DETAIL_KEY_BIGGEST_ELEVATION_GAIN = "biggest-elevation-gain";

    @Autowired
    private Translator translator;

    private final List<TranslatableName> detailNames;

    public StravaStatisticsStreamer(final BinaryStreamer binaryStreamer) {
        super(binaryStreamer);
        injectContext(this);
        this.detailNames = asList(
            new TranslatableName(DETAIL_KEY_NAME, null),
            new TranslatableName(DETAIL_KEY_COUNT, null),
            new TranslatableName(DETAIL_KEY_DISTANCE, null),
            new TranslatableName(DETAIL_KEY_MOVING_TIME, null),
            new TranslatableName(DETAIL_KEY_ELAPSED_TIME, null),
            new TranslatableName(DETAIL_KEY_ELEVATION_GAIN, null),
            new TranslatableName(DETAIL_KEY_ACHIEVEMENT_COUNT, null),
            new TranslatableName(DETAIL_KEY_BIGGEST_DISTANCE, null),
            new TranslatableName(DETAIL_KEY_BIGGEST_ELEVATION_GAIN, null));
    }

    @Override
    public Stream<StravaStatisticStreamer> stream() {
        AthleteStatistics athleteStatistics = binaryStreamer().getAthleteStatistics();
        return Arrays.stream(StravaStatisticsType.values())
            .map(type -> new StravaStatisticStreamer(
                translator.translate(type.getKey()),
                type.getStatisticsSupplier().apply(athleteStatistics)));
    }

    @Override
    public StravaStatisticsJsonStreamer binaryStreamer() {
        return (StravaStatisticsJsonStreamer) super.binaryStreamer();
    }

    @Override
    public String getIconName() {
        return STATISTICS_ICON_NAME;
    }

    @Override
    public String getViewId() {
        return FOLDER_VIEW_ID;
    }

    @Override
    public List<TranslatableName> detailNames() {
        return detailNames;
    }
}
