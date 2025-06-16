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
package org.cosinus.streamer.database;

import org.cosinus.streamer.api.stream.StreamDelegate;
import org.cosinus.streamer.database.resultset.ResultSet;

import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

public class DatabaseStream extends StreamDelegate<ResultSet> {

    private final ResultSet resultSet;

    public DatabaseStream(Stream<ResultSet> delegate, ResultSet resultSet) {
        super(delegate);
        this.resultSet = resultSet;
    }

    public static Stream<ResultSet> of(ResultSet resultSet) {
        DatabaseSpliterator spliterator = new DatabaseSpliterator(resultSet);
        return new DatabaseStream(stream(spliterator, false), resultSet)
            .onClose(spliterator::close);
    }

    @Override
    public void close() {
        super.close();
        resultSet.close();
    }
}
