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

package org.cosinus.streamer.ui.view.map;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.gpx.GpxPoint;
import org.cosinus.streamer.ui.action.execute.load.LoadWorkerModel;
import org.cosinus.streamer.ui.view.Viewer;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.listener.dragdrop.DragAndDrop;
import org.cosinus.swing.listener.dragdrop.DragAndDropAware;
import org.cosinus.swing.listener.dragdrop.DragAndDropListener;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_MITER;
import static java.awt.Color.gray;
import static java.lang.Math.min;
import static java.util.stream.IntStream.range;
import static org.cosinus.swing.image.ImageSettings.QUALITY;

public class MapViewer extends Panel implements Viewer<GpxPoint>, DragAndDropAware, DragAndDropListener {

    public static final Color BACKGROUND_COLOR = new Color(175, 229, 176);

    public static final Color FOREGROUND_COLOR = new Color(252, 76, 2);

    public static final int MINIMUM_SIZE = 30;

    public static final int ZOOM_UNIT_PERCENTAGE = 10;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    private final DragAndDrop dragAndDrop;

    @Getter
    private final MapModel mapModel;

    @Setter
    private boolean active = true;

    public MapViewer() {
        this.mapModel = new MapModel();
        this.dragAndDrop = new DragAndDrop();
    }

    @Override
    public void initComponents() {
        super.initComponents();
        addMouseWheelListener(mouseWheelEvent -> {
            Point mousePoint = mouseWheelEvent.getPoint();
            int direction = mouseWheelEvent.getPreciseWheelRotation() < 0 ? 1 : -1;
            zoom(direction * ZOOM_UNIT_PERCENTAGE, mousePoint);
        });
        addDragAndDropListener(this);
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

        g2d.translate(
            mapModel.getTranslationX() + (dragAndDrop.isDragging() ? dragAndDrop.getX() : 0),
            mapModel.getTranslationY() + (dragAndDrop.isDragging() ? dragAndDrop.getY() : 0));

        g2d.setColor(getForeground());
        g2d.setStroke(new BasicStroke(1, CAP_ROUND, JOIN_MITER));

        range(1, mapModel.getPoints().size())
            .forEach(index -> {
                Point lastPoint = mapModel.getPoints().get(index - 1);
                Point point = mapModel.getPoints().get(index);
                g2d.drawLine(lastPoint.x, lastPoint.y, point.x, point.y);
            });
    }

    private void zoom(int zoom, Point center) {
        if (mapModel.getSize() != null) {
            double percent = zoom / 100d;
            double delta = mapModel.getSize() * percent;
            int size = (int) (mapModel.getSize() + delta);
            if (size > MINIMUM_SIZE) {
                mapModel.setSize(size);
                double translateX = center.x - (double) getWidth() / 2;
                double translateY = center.y - (double) getHeight() / 2;
                double deltaX = (delta / 2) + translateX * percent;
                double deltaY = (delta / 2) + translateY * percent;
                mapModel.setTranslationX(mapModel.getTranslationX() - (int) deltaX);
                mapModel.setTranslationY(mapModel.getTranslationY() - (int) deltaY);
                refresh();
            }
        }
    }

    @Override
    public void drop(DragAndDrop dragAndDrop) {
        mapModel.setTranslationX(mapModel.getTranslationX() + dragAndDrop.getX());
        mapModel.setTranslationY(mapModel.getTranslationY() + dragAndDrop.getY());
    }

    @Override
    public DragAndDrop getDragAndDrop() {
        return dragAndDrop;
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

    @Override
    public void refresh() {
        if (mapModel.getSize() == null) {
            mapModel.setSize(min(getWidth(), getHeight()));
        }
        mapModel.initScale();
        super.refresh();
    }

    @Override
    public void finishLoading(LoadWorkerModel<?> loadWorkerModel) {
        refresh();
    }
}
