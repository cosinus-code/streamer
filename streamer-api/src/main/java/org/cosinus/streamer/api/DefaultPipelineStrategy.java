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

import org.cosinus.stream.error.AbortPipelineConsumeException;
import org.cosinus.stream.error.SkipPipelineConsumeException;
import org.cosinus.stream.pipeline.PipelineStrategy;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.dialog.DialogOption;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.dialog.DialogOption.*;

public class DefaultPipelineStrategy implements PipelineStrategy {

    private boolean skipAll;

    @Autowired
    private SwingApplicationFrame applicationFrame;

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected Translator translator;

    public DefaultPipelineStrategy() {
        injectContext(this);
    }

    @Override
    public boolean shouldRetryOnFail(Exception exception) {
        if (skipAll) {
            throw new SkipPipelineConsumeException();
        }
        DialogOption optionValue = dialogHandler.retryWithSkipDialog(applicationFrame,
            translator.translate("act-streaming-error"));
        if (optionValue == ABORT) {
            throw new AbortPipelineConsumeException("Streaming aborted by user due to error");
        }
        if (optionValue == SKIP_ALL) {
            skipAll = true;
        }
        if (skipAll || optionValue == SKIP) {
            throw new SkipPipelineConsumeException();
        }
        return optionValue == RETRY;
    }
}
