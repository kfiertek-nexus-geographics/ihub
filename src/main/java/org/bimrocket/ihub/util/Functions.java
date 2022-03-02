package org.bimrocket.ihub.util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wilberquito
 */
public class Functions
{
    private static final Logger log = LoggerFactory.getLogger(Functions.class);

    /**
     * Builds a map using two list the first one represent map keys and the
     * second represents map values
     * 
     * To build the map it uses each key value is a representation of indexing
     * lists
     * 
     * @param <T>
     * @param <R>
     * @param keys
     * @param values
     * @return
     */
    public static <T, R> Map<T, R> toMap(List<T> keys, List<R> values)
    {
        Map<T, R> result = new HashMap<>();
        if (keys == null)
            return result;

        for (int i = 0; i < keys.size(); i++)
        {
            result.put(keys.get(i), values.get(i));
        }

        return result;
    }

    /**
     * returns true if all values in map are null
     * 
     * if map is null, returns true as default
     * 
     * @param <K>
     * @param <V>
     * @param dict
     * @return
     */
    public static <K, V> boolean nullableMap(Map<K, V> dict)
    {
        if (dict == null)
            return true;

        int nuls = 0;
        for (Object value : dict.values())
        {
            if (value == null)
                nuls++;
        }
        return nuls == dict.size();
    }

    /**
     * Search for a file alocated in `path` if it is a file tries to delete it
     * 
     * @param path
     */
    public static void deleteFile(String path)
    {
        try
        {
            Files.deleteIfExists(Paths.get(path));
        }
        catch (Exception e)
        {
            log.error(
                    "deleteFile@InventoryUtils: could not delete file - {} - correclty -  exception",
                    path, e.getMessage());
        }
    }

    public static List<String> splitAndTrim(String target, String splitRegex)
    {
        List<String> result = new ArrayList<>();

        if (target == null || splitRegex == null)
            return result;

        var split = target.split(splitRegex);

        for (String item : split)
        {
            result.add(item.trim());
        }

        return result;
    }

    /**
     * builds and uri http request
     * 
     * @param host
     * @param port
     * @param uri
     * @return
     */
    public static URI buildURI(String host, Optional<Integer> port,
            Optional<String> uri, Optional<Map<String, String>> queries)
            throws Exception
    {
        String request = host
                + (port.isEmpty() ? "" : String.format(":%d", port.get()))
                + (uri.isEmpty() ? "" : uri.get());

        if (!request.startsWith("http://") && !request.startsWith("https://"))
        {
            request = "http://".concat(request);
        }

        URIBuilder builder = new URIBuilder(request);

        if (queries.isPresent())
        {
            Map<String, String> M = queries.get();

            for (Entry<String, String> query : M.entrySet())
            {
                builder.setParameter(query.getKey(), query.getValue());
            }
        }

        var URIrequest = builder.build();

        return URIrequest;
    }

}
