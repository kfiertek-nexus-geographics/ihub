package org.bimrocket.ihub.util.consumer;

import java.io.InputStream;

/**
 * 
 * @author wilberquito
 */
public interface IConsumer
{
    public InputStream consum() throws Exception;
}
