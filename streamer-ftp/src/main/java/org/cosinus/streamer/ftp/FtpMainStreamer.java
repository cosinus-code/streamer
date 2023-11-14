package org.cosinus.streamer.ftp;

import org.cosinus.streamer.api.Streamer;
import org.cosinus.streamer.api.meta.MainStreamer;
import org.cosinus.streamer.api.meta.RootStreamer;
import org.cosinus.streamer.ftp.model.FtpModel;
import org.cosinus.streamer.ftp.model.FtpModelProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.swing.image.icon.IconProvider.ICON_NETWORK;

@RootStreamer("FTP")
@ConditionalOnProperty(name = "streamer.ftp.enabled", matchIfMissing = true)
public class FtpMainStreamer extends MainStreamer<FtpStreamer<?>> {

    public static final String FTP_PROTOCOL = "ftp://";

    private final FtpModel ftpModel;

    public FtpMainStreamer(final FtpModelProvider ftpModelProvider) {
        this.ftpModel = ftpModelProvider.getFtpModel();
    }

    @Override
    public Stream<FtpStreamer<?>> stream()
    {
        return ftpModel.getFtpClientNames()
            .stream()
            .map(FtpConnectionStreamer::new);
    }

    @Override
    public boolean isCompatible(String urlPath)
    {
        return false;
    }

    @Override
    public Optional<Streamer> findByUrlPath(String urlPath)
    {
        return Optional.empty();
    }

    @Override
    public void execute(Path path)
    {

    }

    @Override
    public String getIconName() {
        return ICON_NETWORK;
    }

    @Override
    public boolean exists()
    {
        return true;
    }
}
