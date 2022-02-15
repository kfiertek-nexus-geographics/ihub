package org.bimrocket.ihub.interfaces;

import java.util.Optional;

/**
 * 
 * @author wilberquito
 */
public interface BasicClientHandler<T>
{
    T client();

    /**
     * 
     * @param base
     * @param user
     * @param password
     * @param local
     * @param uri
     * @param responseType
     * @throws Exception
     */
    void stage(String base, String user, String password, String local,
            Optional<String> uri, Optional<Integer> responseType)
            throws Exception;

    /**
     * once download is made the connection is closed
     * 
     * @return
     */
    boolean download();
}
