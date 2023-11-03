package org.cosinus.streamer.ui.view.table;

import java.io.File;

public interface ViewItem {

    boolean isTopItem();

    boolean isParent();

    String getName();

    String getValue();

    String getType();

    String getDescription();

    long getSize();

    long getLastModified();

    boolean isLink();

    boolean isHidden();

    String getFormattedSize();

    File toFile();

    String getIconName();
}
