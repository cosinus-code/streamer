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

package org.cosinus.streamer.google.drive.connection;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import org.apache.http.client.HttpResponseException;
import org.cosinus.streamer.api.error.StreamerException;

public class GoogleDriveException extends StreamerException {

    private GoogleJsonResponseException googleJsonResponseException;

    public GoogleDriveException(String message) {
        super(message);
    }

    public GoogleDriveException(String messageKey, Object... messageArguments) {
        super(messageKey, messageArguments);
    }

    public GoogleDriveException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoogleDriveException(Throwable cause) {
        super(cause.getMessage(), cause);
        if (cause instanceof GoogleJsonResponseException responseException) {
            this.googleJsonResponseException = responseException;
        }
    }

    public GoogleJsonResponseException getGoogleJsonResponseException() {
        return googleJsonResponseException;
    }

    public int getStatusCode() {
        return googleJsonResponseException.getStatusCode();
    }
}
