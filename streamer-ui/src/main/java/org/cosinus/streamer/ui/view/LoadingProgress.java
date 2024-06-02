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

import org.cosinus.swing.form.CustomProgressBarUI;
import org.cosinus.swing.form.ProgressBar;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import static org.cosinus.swing.border.Borders.emptyBorder;

public class LoadingProgress extends ProgressBar {

    @Autowired
    private ApplicationUIHandler uiHandler;

    public LoadingProgress() {
        if (uiHandler.isLookAndFeelMac()) {
            setUI(new CustomProgressBarUI());
            setBorder(emptyBorder(0));
        }
    }

    public void startLoading() {
        startLoading(-1);
    }

    public void startLoading(long totalSizeToLoad) {
        if (totalSizeToLoad != 0) {
            setVisible(true);
            if (totalSizeToLoad > 0) {
                setMaximum(100);
            }
        }
    }

    public void updateLoading(long loadedSize, long totalSizeToLoad) {
        if (totalSizeToLoad > 0 && loadedSize > 0) {
            int progress = (int) ((loadedSize * 100) / totalSizeToLoad);
            setIndeterminate(false);
            setValue(progress);
        }
    }

    public void finishLoading() {
        setValue(getMaximum());
        setVisible(false);
        setIndeterminate(true);
        setMaximum(0);
    }
}
