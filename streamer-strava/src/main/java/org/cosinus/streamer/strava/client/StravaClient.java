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

import org.cosinus.streamer.strava.model.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@FeignClient(
    name = "strava",
    path = "/api/v3")
public interface StravaClient {

    @GetMapping("/athlete")
    AthleteProfile getCurrentAthleteProfile();

    @PostMapping("/activities")
    String createActivity(@RequestParam String name, @RequestParam String type, @RequestParam String sportType,
                          @RequestParam("start_date_local") Date startDateLocal, @RequestParam int elapsedTime,
                          @RequestParam String description, @RequestParam float distance,
                          @RequestParam int trainer, @RequestParam int commute);

    @GetMapping("/athlete/activities")
    List<Activity> getActivities(@RequestParam long after, @RequestParam long before,
                                 @RequestParam("per_page") int pageSize, @RequestParam int page);

    @GetMapping("/athletes/{id}/stats")
    AthleteStatistics getAthleteStatistics(@PathVariable long id);

    @GetMapping("/activities/{id}")
    Activity getActivityById(@PathVariable long id, @RequestParam("include_all_efforts") boolean includeAllEfforts);

    @GetMapping("/activities/{id}/laps")
    List<ActivityLap> getActivityLaps(@PathVariable long id);

    @GetMapping("/activities/{id}/kudos")
    List<AthleteName> getActivityKudos(@PathVariable long id);

    @GetMapping("/activities/{id}/comments")
    List<ActivityComment> getActivityComments(@PathVariable long id);

    @PutMapping("/activities/{id}")
    String updateActivity(@PathVariable long id, @RequestBody UpdatableActivity activity);
}
