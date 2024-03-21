package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.Streamable;
import org.cosinus.streamer.ui.view.StreamerEditor;
import org.cosinus.streamer.ui.view.StreamerView;
import org.cosinus.swing.error.ErrorHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.logging.log4j.util.Strings.LINE_SEPARATOR;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class IconCellEditor<T extends Streamable> implements StreamerEditor<T>, FocusListener {

    @Autowired
    private ErrorHandler errorHandler;

    private final IconView<T> view;

    private T itemToBeEdited;

    private final JTextPane nameEditor;

    public IconCellEditor(final IconView<T> view) {
        injectContext(this);

        this.view = view;
        this.nameEditor = new JTextPane();
        nameEditor.addFocusListener(this);
        nameEditor.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    if (e.getKeyCode() == VK_ENTER) {
                        String text = nameEditor.getText().replaceAll(LINE_SEPARATOR, "");
                        itemToBeEdited.updateDetail(0, text);
                        save();
                        setVisible(false);
                    } else if (e.getKeyCode() == VK_ESCAPE) {
                        setVisible(false);
                    }
                } catch (Exception ex) {
                    errorHandler.handleError(getView(), ex);
                }
            }
        });
        nameEditor.validate();
    }

    @Override
    public void loadAndShow(T itemToBeEdited) {
        this.itemToBeEdited = itemToBeEdited;

        view.resetCellEditor(nameEditor);

        nameEditor.setVisible(true);
        nameEditor.requestFocus();

    }

    @Override
    public void save() {
        itemToBeEdited.save();
        view.reload(itemToBeEdited.getName());
    }

    @Override
    public void setVisible(boolean visible) {
        nameEditor.setVisible(visible);
        if (!visible) {
            invokeLater(view::requestFocus);
        }
    }

    public JTextPane getNameEditor() {
        return nameEditor;
    }

    @Override
    public StreamerView<T> getView() {
        return view;
    }

    @Override
    public void focusGained(FocusEvent e) {
        setVisible(true);
        nameEditor.selectAll();
    }

    @Override
    public void focusLost(FocusEvent e) {
        setVisible(false);
    }
}
