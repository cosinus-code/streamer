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

package org.cosinus.streamer.ui.action;

import lombok.Builder;
import lombok.Getter;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.action.execute.ActionModel;

import java.util.List;

@Builder
public class DoHereModel implements ActionModel {

    public static final String DRAG_HERE_ID = "drag-here";

    @Getter
    private StreamerView<?> destinationView;

    @Getter
    private final List<String> paths;

    @Getter
    private boolean useSelectedItemAsDestination;

    @Override
    public String getExecutionId() {
        return DRAG_HERE_ID;
    }

    @Override
    public String getActionId() {
        return DRAG_HERE_ID;
    }
}
