package org.cosinus.streamer.database.connection;

import org.cosinus.streamer.api.error.StreamerException;

public class DatabaseException extends StreamerException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String messageKey, Object... messageArguments) {
        super(messageKey, messageArguments);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
