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
import org.cosinus.streamer.ui.action.execute.copy.CopyActionModel;
import org.cosinus.streamer.ui.error.AbortActionException;
import org.cosinus.streamer.ui.error.ActionException;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.boot.SwingApplicationFrame;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.worker.SwingWorker;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.currentThread;
import static java.util.Optional.ofNullable;

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
    protected WorkerListenerHandler<M> workerListenerHandler;

    @Autowired
    protected ErrorHandler errorHandler;

    private final String id;

    protected final M workerModel;

    private ActionException error;

    private boolean paused;

    protected Worker(String id, M workerModel) {
        this.id = id;
        this.workerModel = workerModel;
    }

    public String getId()
    {
        return id;
    }

    public void start()
    {
        execute();
        workerListenerHandler.workerStarted(getId(), getWorkerModel());
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

//    public void publishProgress(Runnable runnable) {
//        runnable.run();
//        publishProgress();
//    }
//
//    public void publishProgress() {
//        publish(workerModel);
//    }

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
            LOG.trace("Action aborted: " + id);
        }
        return workerModel;
    }

    @Override
    protected void process(List<T> items) {
        checkWorkerStatus();
        workerModel.update(items);
        workerListenerHandler.workerUpdated(getId(), workerModel);
    }

    @Override
    protected void done() {
        try {
            if (!isCancelled()) {
                get();
            }
        } catch (InterruptedException | ExecutionException ex) {
            errorHandler.handleError(applicationFrame, ex);
        }

        ofNullable(error)
            .ifPresent(error -> errorHandler.handleError(applicationFrame, error));

        workerListenerHandler.workerFinished(getId(), workerModel);
        actionExecutors.getActionExecutor(CopyActionModel.class)
            .ifPresent(executor -> executor.remove(getId()));

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
