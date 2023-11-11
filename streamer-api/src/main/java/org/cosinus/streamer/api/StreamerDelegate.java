package org.cosinus.streamer.api;

import java.nio.file.Path;

public abstract class StreamerDelegate<T, S extends Streamer<?>> implements Streamer<T>
{
    protected final S delegate;

    public StreamerDelegate(S delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public BinaryStreamer binaryStreamer()
    {
        return delegate.binaryStreamer();
    }

    @Override
    public ParentStreamer<?> getParent()
    {
        return delegate.getParent();
    }

    @Override
    public void save()
    {
        delegate.save();
    }

    @Override
    public boolean delete()
    {
        return delegate.delete();
    }

    @Override
    public String getProtocol()
    {
        return delegate.getProtocol();
    }

    @Override
    public Path getPath()
    {
        return delegate.getPath();
    }

    @Override
    public boolean exists()
    {
        return delegate.exists();
    }

    @Override
    public long getSize()
    {
        return delegate.getSize();
    }

    @Override
    public long lastModified()
    {
        return delegate.lastModified();
    }

    @Override
    public boolean rename(String newName)
    {
        return delegate.rename(newName);
    }

    @Override
    public boolean canRead()
    {
        return delegate.canRead();
    }

    @Override
    public boolean canWrite()
    {
        return delegate.canWrite();
    }

    @Override
    public String getName()
    {
        return delegate.getName();
    }

    @Override
    public String getType()
    {
        return delegate.getType();
    }

    @Override
    public boolean isLink()
    {
        return delegate.isLink();
    }

    @Override
    public boolean isHidden()
    {
        return delegate.isHidden();
    }

    @Override
    public String getIconName()
    {
        return delegate.getIconName();
    }

    @Override
    public String getValue()
    {
        return delegate.getValue();
    }

    @Override
    public String getDescription()
    {
        return delegate.getDescription();
    }

    @Override
    public String getUrlPath()
    {
        return delegate.getUrlPath();
    }

    @Override
    public boolean isParent()
    {
        return delegate.isParent();
    }

    @Override
    public boolean isOlderThan(Streamer<?> streamerToCompareTo)
    {
        return delegate.isOlderThan(streamerToCompareTo);
    }

    @Override
    public String getId()
    {
        return delegate.getId();
    }

    @Override
    public BinaryStreamer createBinaryStreamer(Path path)
    {
        return delegate.createBinaryStreamer(path);
    }

    @Override
    public boolean isTextCompatible()
    {
        return delegate.isTextCompatible();
    }
}
