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
package org.cosinus.streamer.database.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.api.error.StreamerException;
import org.cosinus.streamer.api.remote.AbstractConnectionFactory;
import org.cosinus.streamer.database.model.DatabaseConnectionModel;
import org.cosinus.streamer.database.model.DatabaseModelProvider;
import org.cosinus.streamer.database.resultset.ResultSet;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Map;

import static java.sql.DriverManager.getConnection;
import static java.util.Optional.ofNullable;


@Component
public class DatabaseConnectionFactory extends AbstractConnectionFactory<String, DatabaseConnection, ResultSet> {

    private static final Logger LOG = LogManager.getLogger(DatabaseConnectionFactory.class);

    private Map<String, DatabaseConnectionModel> connectionModelsMap;

    public DatabaseConnectionFactory(final DatabaseModelProvider databaseModelProvider) {
        this.connectionModelsMap = databaseModelProvider.getConnectionModelsMap();
    }

    @Override
    public DatabaseConnection create(String connectionName) throws Exception {
        //TODO: register driver only if not yet registered
        //DriverManager.registerDriver(new OracleDriver());
        DatabaseConnectionModel databaseConnectionModel = getDatabaseConnectionModel(connectionName);
        Connection connection = getConnection(
            databaseConnectionModel.url(), databaseConnectionModel.username(), databaseConnectionModel.password());
        LOG.info("Connected to database: {}", databaseConnectionModel.url());

        return new DatabaseConnection(connectionName, connection, databaseConnectionModel.username());
    }

    @Override
    public boolean validateConnection(final DatabaseConnection connection) {
        return connection.isValid();
    }

    @Override
    public void destroyConnection(final DatabaseConnection connection) {
        connection.close();
    }


    private DatabaseConnectionModel getDatabaseConnectionModel(String connectionName) {
        return ofNullable(connectionModelsMap.get(connectionName))
            .orElseThrow(() -> new StreamerException("Database connection model not found: " + connectionName));
    }
}
