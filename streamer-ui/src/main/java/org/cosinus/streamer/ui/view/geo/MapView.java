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

package org.cosinus.streamer.ui.view.geo;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.gpx.GpxPoint;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.SwingComponent;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.Color.gray;
import static java.lang.Math.*;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.image.ImageSettings.QUALITY;

public class MapView extends SwingComponent implements Viewer<GpxPoint> {

    public static final Color BACKGROUND_COLOR = new Color(175, 229, 176);

    public static final Color FOREGROUND_COLOR = new Color(252, 76, 2);

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Getter
    private final MapModel mapModel;

    @Setter
    private boolean active = true;

    public MapView() {
        this.mapModel = new MapModel();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        QUALITY.apply(g2d);

        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        if (active) {
            g2d.setColor(getFocusColor());
            g2d.drawRect(0, 0, getWidth(), getHeight());
        }

        int size = min(getWidth(), getHeight());
        int pad = 10;
        int drawable = size - pad * 2;

        g2d.setColor(getForeground());
        g2d.setStroke(new BasicStroke(1, CAP_ROUND, JOIN_MITER));

        double scale = drawable / max(mapModel.getMaxX() - mapModel.getMinX(), mapModel.getMaxY() - mapModel.getMinY());

        double middleX = (drawable - (mapModel.getMaxX() - mapModel.getMinX()) * scale) / 2;
        double middleY = (drawable - (mapModel.getMaxY() - mapModel.getMinY()) * scale) / 2;

        AtomicReference<Point> storedPoint = new AtomicReference<>();
        mapModel
            .stream()
            .map(point -> new Point(
                (int) ((point.getPoint().getLatitude().doubleValue() - mapModel.getMinX()) * scale + middleX + pad),
                (int) ((point.getPoint().getLongitude().doubleValue() - mapModel.getMinY()) * scale + middleY + pad)))
            .forEach(point -> {
                ofNullable(storedPoint.get())
                    .filter(lastPoint -> abs(lastPoint.x - point.x) > 1 ||
                        abs(lastPoint.y - point.y) > 1)
                    .ifPresent(lastPoint -> {
                        g.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
                        storedPoint.set(point);
                    });
                if (storedPoint.get() == null) {
                    storedPoint.set(point);
                }
            });
    }

    @Override
    public Color getBackground() {
        return BACKGROUND_COLOR;
    }

    @Override
    public Color getForeground() {
        return FOREGROUND_COLOR;
    }

    public Color getFocusColor() {
        return gray;
    }

    @Override
    public void reset(Streamer<GpxPoint> parentStreamer) {
        mapModel.reset();
    }
}
