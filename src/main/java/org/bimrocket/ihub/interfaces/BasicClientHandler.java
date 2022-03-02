package org.bimrocket.ihub.interfaces;

import java.util.Map;
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
     * @param hostname
     * @param port
     * @param user
     * @param password
     * @param local
     * @param uri
     * @param responseType
     * @throws Exception
     */
    void stage(String hostname, Optional<Integer> port, String user,
            String password, String local, Optional<String> uri,
            Optional<Integer> responseType,
            Optional<Map<String, String>> parameters) throws Exception;

    /**
     * once download is made the connection is closed
     * 
     * @return
     */
    boolean download();
}
