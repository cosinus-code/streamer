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
import org.cosinus.streamer.strava.model.AthleteProfile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Function;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

@StravaComponent
public class StravaClientInvoker {

    public static final String ATHLETE = "ATHLETE";

    private final StravaClient stravaClient;

    public StravaClientInvoker(final StravaClient stravaClient) {
        this.stravaClient = stravaClient;
    }

    public <T> T invoke(String invokeAsUSer, Function<StravaClient, T> stravaClientCall) {
        SecurityContextHolder.getContext().setAuthentication(
            new AnonymousAuthenticationToken(invokeAsUSer, invokeAsUSer, createAuthorityList(ATHLETE)));
        try {
            return stravaClientCall.apply(stravaClient);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }
}
