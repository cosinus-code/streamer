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

public enum FlatStreamingStrategy
{
    IN_DEPTH(true, true),
    LEVEL_BOTTOM_UP(false, true),
    LEVEL_UP_BOTTOM(true, false);

    private final boolean parentFirst;

    private final boolean depthFirst;

    FlatStreamingStrategy(boolean parentFirst, boolean depthFirst) {
        this.parentFirst = parentFirst;
        this.depthFirst = depthFirst;
    }

    public boolean isParentFirst()
    {
        return parentFirst;
    }

    public boolean isDepthFirst()
    {
        return depthFirst;
    }
}
