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

package org.cosinus.streamer.strava.client;

import org.cosinus.streamer.strava.StravaComponent;
import org.cosinus.streamer.strava.model.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.Date;
import java.util.List;

@StravaComponent
public class StravaCacheClient {

    public static final String STRAVA_CLIENT_CACHE_NAME = "strava-client";

    public static final String STRAVA_KEY_GENERATOR = "stravaKeyGenerator";

    private final StravaClient stravaClient;

    private final CacheManager cacheManager;

    public StravaCacheClient(final StravaClient stravaClient,
                             final CacheManager cacheManager) {
        this.stravaClient = stravaClient;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public AthleteProfile getCurrentAthleteProfile() {
        return stravaClient.getCurrentAthleteProfile();
    }

    public String createActivity(String name, String type, String sportType, Date startDateLocal, int elapsedTime,
                                 String description, float distance, int trainer, int commute) {
        return createActivity(name, type, sportType, startDateLocal, elapsedTime, description, distance, trainer, commute);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<Activity> getCurrentAthleteActivities(long after, long before, int pageSize, int page) {
        return stravaClient.getCurrentAthleteActivities(after, before, pageSize, page);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<Activity> getCurrentAthleteFriends(int pageSize, int page) {
        return stravaClient.getCurrentAthleteFriends(pageSize, page);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<Club> getCurrentAthleteClubs(int pageSize, int page) {
        return stravaClient.getCurrentAthleteClubs(pageSize, page);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public AthleteStatistics getAthleteStatistics(long id) {
        return stravaClient.getAthleteStatistics(id);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public Activity getActivityById(long id, boolean includeAllEfforts) {
        return stravaClient.getActivityById(id, includeAllEfforts);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<ActivityLap> getActivityLaps(long id) {
        return stravaClient.getActivityLaps(id);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<AthleteName> getActivityKudos(long id) {
        return stravaClient.getActivityKudos(id);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public List<ActivityComment> getActivityComments(long id) {
        return stravaClient.getActivityComments(id);
    }

    @Cacheable(value = STRAVA_CLIENT_CACHE_NAME, keyGenerator = STRAVA_KEY_GENERATOR)
    public ActivityStreams getActivityStreams(long id, String[] keys, boolean keyByType) {
        return stravaClient.getActivityStreams(id, keys, keyByType);
    }

    public String updateActivity(long id, UpdatableActivity activity) {
        return stravaClient.updateActivity(id, activity);
    }

    public void evict(String userName) {
        Cache cache = cacheManager.getCache(STRAVA_CLIENT_CACHE_NAME);
        if (cache instanceof ConcurrentMapCache mapCache) {
            mapCache.getNativeCache().keySet().removeIf(
                key -> key.toString().contains(userName)
            );
        }
    }
}
