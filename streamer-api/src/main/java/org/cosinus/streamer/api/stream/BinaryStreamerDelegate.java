package org.cosinus.streamer.api.stream;

import org.cosinus.streamer.api.BinaryStreamer;
import org.cosinus.streamer.api.ParentStreamer;
import org.cosinus.streamer.api.StreamerDelegate;

import java.io.InputStream;
import java.io.OutputStream;

public class BinaryStreamerDelegate extends StreamerDelegate<byte[], BinaryStreamer> implements BinaryStreamer {

    private ParentStreamer<?> parent;

    public BinaryStreamerDelegate(BinaryStreamer delegate) {
        super(delegate);
    }

    public BinaryStreamerDelegate(BinaryStreamer delegate, ParentStreamer<?> parent) {
        super(delegate);
        this.parent = parent;
    }

    @Override
    public ParentStreamer<?> getParent() {
        if (parent != null) {
            return parent;
        }
        return super.getParent();
    }

    @Override
    public InputStream inputStream() {
        return delegate.inputStream();
    }

    @Override
    public OutputStream outputStream(boolean append) {
        return delegate.outputStream(append);
    }
}
