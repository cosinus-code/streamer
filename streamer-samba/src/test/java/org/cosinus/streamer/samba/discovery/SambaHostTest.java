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

package org.cosinus.streamer.samba.discovery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SambaHostTest {

    private final SambaHost namedHost = new SambaHost("192.168.1.10", "nas.fritz.box");

    private final SambaHost unnamedHost = new SambaHost("192.168.1.11", "192.168.1.11");

    @Test
    public void testShortName() {
        assertEquals("nas", namedHost.shortName());
        assertEquals("192.168.1.11", unnamedHost.shortName());
        assertEquals("nas", new SambaHost("192.168.1.10", "nas").shortName());
    }

    @Test
    public void testMatches() {
        assertTrue(namedHost.matches("192.168.1.10"));
        assertTrue(namedHost.matches("nas.fritz.box"));
        assertTrue(namedHost.matches("NAS"));
        assertFalse(namedHost.matches("192.168.1.11"));
        assertFalse(namedHost.matches("other"));
        assertFalse(namedHost.matches(null));
        assertTrue(unnamedHost.matches("192.168.1.11"));
    }
}
