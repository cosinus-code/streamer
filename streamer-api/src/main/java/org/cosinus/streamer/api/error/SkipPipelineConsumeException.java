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

package org.cosinus.streamer.api.error;

public class SkipPipelineConsumeException extends RuntimeException {

    private long skippedSize;

    public SkipPipelineConsumeException(long skippedSize) {
        this.skippedSize = skippedSize;
    }

    public SkipPipelineConsumeException(long skippedSize, String message) {
        super(message);
        this.skippedSize = skippedSize;
    }

    public long getSkippedSize() {
        return skippedSize;
    }
}
