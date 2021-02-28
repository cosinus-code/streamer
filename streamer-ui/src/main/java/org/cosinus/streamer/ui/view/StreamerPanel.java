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

package org.cosinus.streamer.ui.view;

import org.cosinus.swing.form.Panel;

import java.awt.*;

import static java.awt.BorderLayout.CENTER;

public class StreamerPanel extends Panel {

    private final StreamerView view;

    public StreamerPanel(StreamerView view) {
        this.view = view;
    }

    @Override
    public void initComponents() {
        setLayout(new BorderLayout());
        add(view, CENTER);
    }

    public void initContent() {
        view.initContent();
        view.initDataContent();
    }

    public void updateForm() {
        view.updateForm();
    }

    public StreamerView getView() {
        return view;
    }

    @Override
    public void translate() {
        view.translate();
    }
}
