package org.cosinus.streamer.ftp;

import org.apache.commons.net.ftp.FTPFile;

import java.nio.file.Paths;

import static org.cosinus.swing.image.icon.IconProvider.ICON_FILE_SERVER;

public class FtpConnectionStreamer extends FtpParentStreamer {

    public FtpConnectionStreamer(String connectionName) {
        super(new FTPFile(), Paths.get(connectionName), connectionName);
    }

    @Override
    public String getIconName() {
        return ICON_FILE_SERVER;
    }
}
