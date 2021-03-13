/*
 * Copyright 2020 Cosinus Software
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

package org.cosinus.streamer.ui.action.execute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.streamer.ui.action.progress.ProgressListenerHandler;
import org.cosinus.streamer.ui.action.progress.ProgressModel;
import org.cosinus.streamer.ui.error.ActionCancelledException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.streamer.ui.error.SkipActionException;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.dialog.DialogOption;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Abstract {@link javax.swing.SwingWorker} with custom progress
 */
public abstract class SwingProgressWorker<P extends ProgressModel> extends SwingWorker<Void, P> {

    private static final Logger LOG = LogManager.getLogger(SwingProgressWorker.class);

    @Autowired
    protected ProgressListenerHandler<P> progressListenerHandler;

    @Autowired
    protected DialogHandler dialogHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    protected final Window parentWindow;

    private final String actionId;

    protected final P progress;

    private ActionException error;

    private boolean paused;

    protected SwingProgressWorker(Window parentWindow,
                                  String actionId,
                                  P progress) {
        this.parentWindow = parentWindow;
        this.actionId = actionId;
        this.progress = progress;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    protected void setProgress(P progress) {
        publish(progress);
    }

    protected void setError(ActionException error) {
        this.error = error;
    }

    @Override
    protected Void doInBackground() {
        try {
            doWork();
        } catch (ActionException ex) {
            setError(ex);
        } catch (ActionCancelledException ex) {
            LOG.trace("Action cancelled: " + actionId);
        }
        return null;
    }

    @Override
    protected void process(List<P> progress) {
        if (!isCancelled()) {
            progressListenerHandler.setProgress(progress.get(progress.size() - 1));
        }
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to update progress", e);
        }

        Optional.ofNullable(error)
            .ifPresent(error -> errorHandler.handleError(parentWindow, error));
        progressListenerHandler.finishProgress();
    }

    protected void checkActionStatus() {
        if (isCancelled()) {
            throw new ActionCancelledException();
        }
        if (isPaused()) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    protected boolean retryOrSkip(String message) {
        DialogOption optionValue = dialogHandler.retryWithSkipDialog(parentWindow, message);
        if (optionValue == DialogOption.ABORT) {
            throw new ActionCancelledException();
        }
        if (optionValue == DialogOption.SKIP) {
            throw new SkipActionException();
        }
        return optionValue == DialogOption.RETRY;
    }

    protected abstract void doWork();
}
