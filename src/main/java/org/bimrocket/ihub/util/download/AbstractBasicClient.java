package org.bimrocket.ihub.util.download;

import org.bimrocket.ihub.interfaces.BasicClientHandler;

public abstract class AbstractBasicClient<T> implements BasicClientHandler<T>
{
    protected T client;

    @Override
    public T client()
    {
        return client;
    }

    protected abstract void reset();
}
