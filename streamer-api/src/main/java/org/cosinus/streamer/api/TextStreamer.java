package org.cosinus.streamer.api;

import java.util.stream.Stream;

import static org.cosinus.streamer.api.stream.text.TextStream.lines;

public class TextStreamer extends StreamerDelegate<String, BinaryStreamer> implements Streamer<String>
{
    public TextStreamer(BinaryStreamer delegate)
    {
        super(delegate);
    }

    @Override
    public Stream<? extends String> stream()
    {
        return lines(delegate.inputStream());
    }

    @Override
    public BinaryStreamer binaryStreamer()
    {
        return delegate;
    }

    @Override
    public boolean isTextCompatible()
    {
        return true;
    }
}
