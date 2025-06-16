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

package org.cosinus.streamer.pack.compress;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static java.util.Optional.empty;

@Component
public class CompressorInputStreamFactory extends CompressorStreamFactory {

    private static final Logger LOG = LogManager.getLogger(CompressorInputStreamFactory.class);

    @Override
    public CompressorInputStream createCompressorInputStream(String compressorName, InputStream inputStream) {
        try {
            return super.createCompressorInputStream(compressorName, inputStream);
        } catch (CompressorException e) {
            throw new RuntimeException("Cannot create compressor of type: " + compressorName, e);
        }
    }

    @Override
    public CompressorInputStream createCompressorInputStream(InputStream inputStream) {
        try {
            return super.createCompressorInputStream(inputStream);
        } catch (CompressorException e) {
            throw new RuntimeException("Cannot create compressor", e);
        }
    }

    public Optional<String> detectCompressorName(String extension, InputStream inputStream) {
        return getInputStreamCompressorNames()
            .stream()
            .filter(compressorName -> compressorName.equals(extension))
            .findFirst()
            .or(() -> {
                try (InputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
                    return Optional.of(detect(bufferedInputStream));
                } catch (IOException | CompressorException e) {
                    LOG.trace(e);
                    return empty();
                }
            });
    }

    public CompressMetadata createMetadata(CompressorInputStream compressorInputStream) {
        CompressMetadata metadata = new CompressMetadata();
        if (compressorInputStream instanceof GzipCompressorInputStream) {
            GzipParameters gzipParameters = ((GzipCompressorInputStream) compressorInputStream).getMetaData();
            metadata.setName(gzipParameters.getFilename());
            metadata.setModificationTime(gzipParameters.getModificationTime());
        }

        return metadata;
    }
}
