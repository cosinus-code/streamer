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

package org.cosinus.streamer.api;

import org.cosinus.stream.StreamingStrategy;
import org.cosinus.stream.error.AbortPipelineConsumeException;
import org.cosinus.stream.error.SkipPipelineConsumeException;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.dialog.DialogOption;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class DefaultStreamingStrategy implements StreamingStrategy {

    @Autowired
    private SwingApplicationFrame applicationFrame;

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected Translator translator;

    public DefaultStreamingStrategy() {
        injectContext(this);
    }

    @Override
    public boolean shouldRetryOnFail(Exception exception) {
        DialogOption optionValue = dialogHandler.retryWithSkipDialog(applicationFrame,
            translator.translate("act-streaming-error"));
        if (optionValue == DialogOption.ABORT) {
            throw new AbortPipelineConsumeException("Streaming aborted by user");
        }
        if (optionValue == DialogOption.SKIP) {
            //TODO
            throw new SkipPipelineConsumeException(1);
        }
        return optionValue == DialogOption.RETRY;
    }
}
