package org.bimrocket.ihub.interfaces;

import java.util.Map;

/**
 * 
 * @author wilberquito
 */
public interface BasicClientHandler<T>
{
    T builder(String base, String user, String password,
            Map<String, Object> extras) throws Exception;

    boolean download(T client, String uri, String local);
}
