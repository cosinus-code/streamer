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
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.ofNullable;

/**
 * Abstract {@link javax.swing.SwingWorker} with custom progress
 */
public abstract class ProgressWorker<P extends ProgressModel> extends SwingWorker<Void, P> {

    private static final Logger LOG = LogManager.getLogger(ProgressWorker.class);

    @Autowired
    protected ProgressListenerHandler<P> progressListenerHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    protected final Window parentWindow;

    private final String actionId;

    protected final P progress;

    private ActionException error;

    private boolean paused;

    protected ProgressWorker(Window parentWindow,
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

    public void publishProgress() {
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
        } catch (AbortActionException ex) {
            LOG.trace("Action aborted: " + actionId);
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

        ofNullable(error)
            .ifPresent(error -> errorHandler.handleError(parentWindow, error));
        progressListenerHandler.finishProgress(progress);
    }

    public void checkWorkerStatus() {
        if (isCancelled()) {
            throw new AbortActionException("Worker aborted by user");
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

    public P getProgressModel() {
        return progress;
    }

    public Window getParentWindow() {
        return parentWindow;
    }

    protected abstract void doWork();
}
