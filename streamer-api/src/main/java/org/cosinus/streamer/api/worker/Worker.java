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

package org.cosinus.streamer.api.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import error.AbortActionException;
import error.ActionException;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.action.execute.ActionModel;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.currentThread;

/**
 * Abstract {@link javax.swing.SwingWorker} with custom progress
 */
public abstract class Worker<M extends WorkerModel<T>, T> extends SwingWorker<M, T> {

    private static final Logger LOG = LogManager.getLogger(Worker.class);

    @Autowired
    private SwingApplicationFrame applicationFrame;

    @Autowired
    protected ActionExecutors actionExecutors;

    @Autowired
    protected WorkerListenerHandler workerListenerHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    private final String id;

    private final ActionModel actionModel;

    protected final M workerModel;

    private ActionException error;

    private boolean paused;

    protected Worker(ActionModel actionModel, M workerModel) {
        this.id = actionModel.getExecutionId();
        this.actionModel = actionModel;
        this.workerModel = workerModel;
    }

    public String getId() {
        return id;
    }

    public void start() {
        workerListenerHandler.workerStarted(getId(), getWorkerModel());
        execute();
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    protected void setError(ActionException error) {
        this.error = error;
    }

    @Override
    protected M doInBackground() {
        try {
            doWork();
        } catch (ActionException ex) {
            setError(ex);
        } catch (AbortActionException ex) {
            logUserAbort();
        }
        return workerModel;
    }

    @Override
    protected void process(List<T> items) {
        try {
            checkWorkerStatus();
            workerModel.update(items);
            workerListenerHandler.workerUpdated(getId(), workerModel);
        } catch (ActionException ex) {
            setError(ex);
        } catch (AbortActionException ex) {
            logUserAbort();
        }
    }

    protected void logUserAbort() {
        LOG.trace("Action aborted: {}", id);
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get();
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } catch (ExecutionException ex) {
            errorHandler.handleError(applicationFrame, ex);
        }

        if (error != null) {
            LOG.error("Error while worker run", error);
            errorHandler.handleError(applicationFrame, error.getLocalizedMessage());
        }

        onWorkerDoneBeforeFinishing();
        workerListenerHandler.workerFinished(getId(), workerModel);
        actionExecutors.getActionExecutor(actionModel)
            .ifPresent(executor -> executor.remove(getId()));

    }

    protected void onWorkerDoneBeforeFinishing() {
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
                    currentThread().interrupt();
                }
            }
        }
    }

    public M getWorkerModel() {
        return workerModel;
    }

    protected abstract void doWork();
}
