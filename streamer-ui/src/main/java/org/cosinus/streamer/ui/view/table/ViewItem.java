package org.cosinus.streamer.ui.view.table;

import java.io.File;

public interface ViewItem {

    boolean isTopItem();

    String getName();

    boolean isLink();

    boolean isHidden();

    String getFormattedSize();

    File toFile();

    String getIconName();
}
