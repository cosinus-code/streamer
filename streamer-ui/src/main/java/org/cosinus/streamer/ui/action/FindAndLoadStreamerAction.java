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
package org.cosinus.streamer.ui.action;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.action.execute.find.FindActionModel;
import org.cosinus.streamer.ui.action.execute.load.LoadActionModel;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.streamer.ui.view.StreamerViewHandler;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.streamer.ui.action.execute.find.FindActionModel.finaStreamerAndDo;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class FindAndLoadStreamerAction implements Runnable {

    @Autowired
    private ActionExecutors actionExecutors;

    @Autowired
    private StreamerViewHandler streamerViewHandler;

    @Autowired
    private DialogHandler dialogHandler;

    @Autowired
    private Translator translator;

    private final Supplier<String> urlPathSupplier;

    public FindAndLoadStreamerAction(Supplier<String> urlPathSupplier) {
        injectContext(this);
        this.urlPathSupplier = urlPathSupplier;
    }

    @Override
    public void run() {
        ofNullable(urlPathSupplier.get())
            .filter(not(String::isBlank))
            .map(this::addProtocolIfMissing)
            .map(this::createFindActionModel)
            .ifPresentOrElse(actionExecutors::execute, this::showNoStreamerFoundMessage);
    }

    private FindActionModel createFindActionModel(String urlPath) {
        return finaStreamerAndDo(streamerViewHandler.getCurrentLocation(), urlPath, this::loadStreamer);
    }

    private void loadStreamer(Streamer<?> streamerToLoad) {
        ofNullable(streamerToLoad)
            .map(this::createLoadActionModel)
            .ifPresentOrElse(actionExecutors::execute, this::showNoStreamerFoundMessage);
    }

    private void showNoStreamerFoundMessage() {
        dialogHandler.showInfo(translator.translate("no-streamer-found", urlPathSupplier.get()));
    }

    private LoadActionModel<?> createLoadActionModel(Streamer<?> streamerToLoad) {
        return new LoadActionModel<>(streamerViewHandler.getCurrentLocation(), streamerToLoad, null);
    }

    private String addProtocolIfMissing(String urlPath) {
        return ofNullable(urlPath)
            .filter(not(url -> url.contains("://")))
            .flatMap(url -> ofNullable(streamerViewHandler.getCurrentView())
                .map(StreamerView::getParentStreamer)
                .map(Streamer::getProtocol)
                .map(protocol -> protocol.concat(url)))
            .orElse(urlPath);
    }

    public static FindAndLoadStreamerAction findAndLoadStreamer(Supplier<String> urlPathSupplier) {
        return new FindAndLoadStreamerAction(urlPathSupplier);
    }
}
