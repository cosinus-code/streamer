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
package org.cosinus.streamer.api.stream.text;

import org.cosinus.streamer.api.stream.consumer.StreamConsumer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;

public class TextStreamConsumer implements StreamConsumer<String>
{
    private final Writer writer;

    public TextStreamConsumer(OutputStream outputStream) {
        this.writer = new BufferedWriter(new OutputStreamWriter(outputStream));
    }

    @Override
    public void accept(String text) {
        try
        {
            writer.write(text);
        }
        catch (IOException ex)
        {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
