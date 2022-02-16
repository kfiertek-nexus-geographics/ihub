package org.bimrocket.ihub.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wilberquito
 */
public class Functions
{
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
}
