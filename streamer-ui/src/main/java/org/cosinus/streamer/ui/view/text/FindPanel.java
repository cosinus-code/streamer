package org.cosinus.streamer.ui.view.text;

import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.form.control.TextField;
import org.cosinus.swing.form.control.ToggleButton;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.EAST;
import static org.cosinus.swing.border.Borders.emptyBorder;

public class FindPanel extends Panel {

    @Autowired
    private Translator translator;

    private TextField findTextField;

    public FindPanel() {
        super(new BorderLayout(0, 0));
    }

    @Override
    public void initComponents() {
        findTextField = new TextField();

        ToggleButton findCaseSensitiveButton = new ToggleButton("Cc", false);
        ToggleButton findWordButton = new ToggleButton("W", false);
        ToggleButton findByRegularExpressionButton = new ToggleButton(".*", false);

//        findCaseSensitiveButton.setBorder(emptyBorder(0, 5, 0, 2));
//        findWordButton.setBorder(emptyBorder(0, 2, 0, 2));
//        findByRegularExpressionButton.setBorder(emptyBorder(0, 2, 0, 5));

        Label findResultLabel = new Label();
        findResultLabel.setText(translator.translate("find-no-results"));

        Button findPreviousButton = new Button("↑");
        Button findNextButton = new Button("↓");

        Panel buttonsPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonsPanel.add(findCaseSensitiveButton);
        buttonsPanel.add(findWordButton);
        buttonsPanel.add(findByRegularExpressionButton);

        buttonsPanel.add(findResultLabel);
        buttonsPanel.add(findPreviousButton);
        buttonsPanel.add(findNextButton);

        add(findTextField, CENTER);
        add(buttonsPanel, EAST);

        setVisible(false);
    }

    public String getTextToFind() {
        return findTextField.getText();
    }
}
