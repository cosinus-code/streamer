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

package org.cosinus.streamer.api.stream.pipeline;

public interface PipelineListener<D> {

    default void onPreparingPipeline(long preparedDataSize) {
    }

    default void beforePipelineOpen() {
    }

    default void afterPipelineOpen() {
    }

    default void beforePipelineDataConsume(D data) {
    }

    default void afterPipelineDataConsume(D data) {
    }

    default void afterPipelineDataSkip(long skippedDataSize) {
    }

    default void beforePipelineClose() {
    }

    default void afterPipelineClose(boolean pipelineFailed) {
    }

    default void onPipelineFail() {
    }
}
