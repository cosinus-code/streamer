package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

import javax.swing.*;

public class IconView<T extends Streamable> extends TableStreamerView<T> {

    public static final String ICON_VIEW_NAME = "icon";

    private IconCellEditor<T> iconCellEditor;

    public IconView(PanelLocation location) {
        super(location);
    }

    @Override
    public void initComponents() {
        super.initComponents();
    }

    @Override
    public String getName() {
        return ICON_VIEW_NAME;
    }

    @Override
    protected IconTable<T> createDataTable() {
        return new IconTable<>();
    }

    @Override
    protected StreamerEditor<T> createStreamerEditor() {
        if (iconCellEditor == null) {
            iconCellEditor = new IconCellEditor<>(this);
            getContainer().add(iconCellEditor.getNameEditor());
        }
        return iconCellEditor;
    }

    public JTextPane resetCellEditor(JTextPane nameEditor) {
        int index = table.getCurrentIndex();
        int row = table.getTableModel().getRowForIndex(index);
        int column = table.getTableModel().getColumnForIndex(index);

        return ((IconCellRenderer) table.getCellRenderer(row, column)).resetCellEditor(nameEditor, row, column);
    }
}
