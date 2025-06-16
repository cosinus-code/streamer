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
package org.cosinus.streamer.database.resultset;

import org.cosinus.streamer.api.value.*;
import org.cosinus.streamer.database.connection.DatabaseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;
import java.util.function.Function;

import static java.sql.Types.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

public enum ValueResolver {

    TEXT(ResultSet::getString, TextValue::new, VARCHAR, LONGVARCHAR),
    BOOLEAN(ResultSet::getBoolean, BooleanValue::new, BIT),
    INT(ResultSet::getInt, IntegerValue::new, TINYINT, SMALLINT, INTEGER, BIGINT),
    DOUBLE(ResultSet::getDouble, DoubleValue::new, FLOAT, Types.DOUBLE),
    DATE(ResultSet::getDate, DateValue::new, Types.DATE, TIME, TIMESTAMP),
    DECIMAL(ResultSet::getBigDecimal, BigDecimalValue::new, NUMERIC, Types.DECIMAL);

    private final ResultSetValueByIndexSupplier<?> valueSupplier;

    private final Function<Object, Value> valueCreator;

    private final Set<Integer> sqlTypes;

    ValueResolver(final ResultSetValueByIndexSupplier<?> valueSupplier,
                  final Function<Object, Value> valueCreator,
                  int... sqlTypes) {
        this.valueSupplier = valueSupplier;
        this.valueCreator = valueCreator;
        this.sqlTypes = stream(sqlTypes).boxed().collect(toSet());
    }

    Value resolveValue(ResultSet rs, int index) {
        try {
            return valueCreator.apply(valueSupplier.supply(rs, index));
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public static ValueResolver of(int sqlType) {
        return stream(values())
            .filter(type -> type.sqlTypes.contains(sqlType))
            .findFirst()
            .orElse(null);
    }
}
