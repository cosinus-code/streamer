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

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.value.TextValue;
import org.cosinus.streamer.strava.StravaJsonStreamer;
import org.cosinus.streamer.strava.StravaUserStreamer;
import org.cosinus.streamer.strava.client.StravaClientInvoker;
import org.cosinus.streamer.strava.model.AthleteStatistic;
import org.cosinus.streamer.strava.model.AthleteStatistics;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static org.cosinus.streamer.strava.statististics.StravaStatisticsExpander.STRAVA_STATISTICS;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class StravaStatisticsJsonStreamer extends StravaJsonStreamer {

    public static final String STATISTICS = "Statistics";

    public static final String STATISTICS_ICON_NAME = "mate-power-statistics";

    @Autowired
    protected StravaClientInvoker stravaClientInvoker;

    @Autowired
    private Translator translator;

    protected final StravaUserStreamer stravaUserStreamer;

    protected final String userName;

    protected AthleteStatistics athleteStatistics;

    public StravaStatisticsJsonStreamer(final StravaUserStreamer stravaUserStreamer) {
        super(stravaUserStreamer);
        injectContext(this);
        this.stravaUserStreamer = stravaUserStreamer;
        this.userName = stravaUserStreamer.getName();
    }

    @Override
    public ParentStreamer<?> getParent() {
        return stravaUserStreamer;
    }

    @Override
    public String getName() {
        return STATISTICS;
    }

    @Override
    public Path getPath() {
        return getParent().getPath().resolve(STATISTICS);
    }

    @Override
    public String getType() {
        return STRAVA_STATISTICS;
    }

    public AthleteStatistics getAthleteStatistics() {
        if (athleteStatistics == null) {
            athleteStatistics = stravaClientInvoker.invoke(userName,
                stravaClient -> stravaClient.getAthleteStatistics(stravaUserStreamer.getUserId()));
            athleteStatistics.getAllRideStatistics()
                .setBiggestDistance(athleteStatistics.getBiggestRideDistance());
            athleteStatistics.getAllRideStatistics()
                .setBiggestElevationGain(athleteStatistics.getBiggestClimbElevationGain());

            athleteStatistics.setRecentActivitiesStatistics(aggregateAthleteStatistics(
                athleteStatistics.getRecentRideStatistics(),
                athleteStatistics.getRecentRunStatistics(),
                athleteStatistics.getRecentSwimStatistics()
            ));

            athleteStatistics.setYearToDateActivitiesStatistics(aggregateAthleteStatistics(
                athleteStatistics.getYearToDateRideStatistics(),
                athleteStatistics.getYearToDateRunStatistics(),
                athleteStatistics.getYearToDateSwimStatistics()
            ));

            athleteStatistics.setAllActivitiesStatistics(aggregateAthleteStatistics(
                athleteStatistics.getAllRideStatistics(),
                athleteStatistics.getAllRunStatistics(),
                athleteStatistics.getAllSwimStatistics()
            ));
        }
        return athleteStatistics;
    }

    private AthleteStatistic aggregateAthleteStatistics(AthleteStatistic... athleteStatistics) {
        AthleteStatistic aggregateAthleteStatistics = new AthleteStatistic();
        aggregateAthleteStatistics.setCount(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getCount)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum());
        aggregateAthleteStatistics.setDistance(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getDistance)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        aggregateAthleteStatistics.setMovingTime(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getMovingTime)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum());
        aggregateAthleteStatistics.setElapsedTime(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getElapsedTime)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum());
        aggregateAthleteStatistics.setElevationGain(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getElevationGain)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        aggregateAthleteStatistics.setBiggestDistance(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getBiggestDistance)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());
        aggregateAthleteStatistics.setBiggestElevationGain(
            Arrays.stream(athleteStatistics)
                .map(AthleteStatistic::getBiggestElevationGain)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum());

        return aggregateAthleteStatistics;
    }

    @Override
    protected void initDetails() {
        details = singletonList(new TextValue(getName()));
    }

    @Override
    public void reset() {
        athleteStatistics = null;
    }

    @Override
    public Object getSource() {
        return getAthleteStatistics();
    }

    @Override
    public String getDescription() {
        return translator.translate("strava-athlete-statistics");
    }
}
