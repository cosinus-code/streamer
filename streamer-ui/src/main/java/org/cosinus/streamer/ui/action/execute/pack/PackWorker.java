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
package org.cosinus.streamer.ui.action.execute.pack;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.expand.ExpandedStreamer;
import org.cosinus.streamer.api.stream.consumer.StreamConsumer;
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.action.execute.copy.CopyStrategy;
import org.cosinus.streamer.ui.action.execute.copy.CopyWorker;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import static java.util.Optional.ofNullable;

public class PackWorker<S extends Streamer<S>, T extends ExpandedStreamer<T>> extends CopyWorker<S, T> {

    public PackWorker(CopyActionModel copyModel) {
        super(copyModel);
    }

    protected T getDestination() {
        return (T) destination;
    }

    @Override
    public StreamConsumer openPipelineOutputStream(CopyStrategy pipelineStrategy) {
        return ofNullable(getDestination().outputStream(false))
            .map(PackStreamConsumer::new)
            .orElse(null);
    }

    @Override
    protected Path getTargetPath(S streamerToCopy) {
        return super.getRelativePath(streamerToCopy);
    }

    private class PackStreamConsumer implements StreamConsumer<S> {

        private final OutputStream output;

        PackStreamConsumer(final OutputStream output) {
            this.output = output;
        }

        @Override
        public void accept(S streamerToCopy) {
            copyStreamer(streamerToCopy);
        }

        @Override
        public void close() throws IOException {
            output.close();
        }
    }
}
