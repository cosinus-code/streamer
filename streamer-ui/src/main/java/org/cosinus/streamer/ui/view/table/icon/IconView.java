package org.cosinus.streamer.ui.view.table.icon;

import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.ui.view.PanelLocation;
import org.cosinus.streamer.ui.view.table.TableStreamerView;

public class IconView<S extends Streamer<S>> extends TableStreamerView<S> {

    public static final String ICON_VIEW_NAME = "icon";

    public IconView(PanelLocation location, ParentStreamer<S> parentStreamer) {
        super(location, parentStreamer);
    }

    @Override
    public String getName() {
        return ICON_VIEW_NAME;
    }

    @Override
    protected IconTable<S> createDataTable() {
        return new IconTable<>();
    }

//    public Rectangle getRectangle(int index) {
//        Rectangle rect = super.getRectangle(index);
//        boolean preview = Maestro.getOptions().getBooleanOption(Options.OPTION_PREVIEW);
//        //int offsety = Maestro.isWindows() || preview ? 7 : -9;
//        int offsety = Maestro.isWindows() || preview ? -32 : -9;
//        if(Maestro.isLafGTK() && !preview) offsety = -18;
//        int iconHeight = ((IconTable)table).getCellHeight() + offsety;
//        Rectangle rect1 = new Rectangle(rect.x - 17, rect.y + iconHeight, rect.width + 15, rect.height - iconHeight);
//        //return new Rectangle(rect.x - 17, rect.y + ((IconTable)table).getIconSize() + offsety, rect.width + 15, heightRename);
//
//        Insets insets = txtRename.getBorder().getBorderInsets(txtRename);
//
//        int renameX = rect1.x + 2;
//        int renameY = rect1.y;
//        int renameWidth = rect1.width - 7;
//        int renameHeight = rect1.height;
//        int insideWidth = renameWidth - insets.left - insets.right - (Maestro.isLafGTK() ? 0 : 1);
//
//        StringBuffer new_text = new StringBuffer();
//        renameHeight = Utils.wrapWordText(getCurrentStreamer().toString(), new_text, insideWidth, txtRename, false, false);
//        txtRename.setText(new_text.toString());
//
//        return new Rectangle(renameX, renameY, renameWidth + 1, renameHeight + 1);
//    }
//
//    protected JTextComponent getRenameComponent(){
//        JTextPane txt = new JTextPane();
//        StyledDocument doc = txt.getStyledDocument();
//        MutableAttributeSet standard = new SimpleAttributeSet();
//        StyleConstants.setAlignment(standard, StyleConstants.ALIGN_CENTER);
//        StyleConstants.setFontFamily(standard, Maestro.getGeneralFont().getFamily());
//        StyleConstants.setFontSize(standard, Maestro.getGeneralFont().getSize());
//        StyleConstants.setBold(standard, Maestro.getGeneralFont().isBold());
//        StyleConstants.setItalic(standard, Maestro.getGeneralFont().isItalic());
//        doc.setParagraphAttributes(0, 0, standard, true);
//        return txt;
//    }
//
//    protected String getRenameText(){
//        String text = txtRename.getText();
//        StringBuffer new_text = new StringBuffer();
//        for (int i = 0; i < text.length(); i++) {
//            char ch = text.charAt(i);
//            if(ch == '\n') continue;
//            new_text.append(ch);
//        }
//        return new_text.toString();
//    }
}
