
/**
 * 
 * @author wilberquito
 */
package org.bimrocket.ihub.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.interfaces.BasicClientHandler;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.ExcelMapping;
import org.bimrocket.ihub.util.Functions;
import org.bimrocket.ihub.util.download.FTPDownload;
import org.bimrocket.ihub.util.download.HTTPDownload;
import org.python.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author wilberquito
 */
public class ExcelLoaderProcessor extends FullScanLoader
{
    private static final Logger log = LoggerFactory
            .getLogger(ExcelLoaderProcessor.class);

    @ConfigProperty(name = "source.has.headers", description = "First row should be treated as headers?", required = true)
    boolean hasHeaders = true;

    @ConfigProperty(name = "source.protocol", description = "Source protocol to rescue excel { HTTP, FTP }", required = true)
    String protocol;

    @ConfigProperty(name = "source.auth", description = "Type of authentication currently only { BASIC } is supported and it's taken as default", defaultValue = "BASIC")
    String auth;

    @ConfigProperty(name = "source.user", description = "Source user ID", defaultValue = "admin")
    String user;

    @ConfigProperty(name = "source.password", description = "Source user password", defaultValue = "admin")
    String password;

    @ConfigProperty(name = "source.hostname", description = "IP or DNS", required = true)
    String hostname;

    @ConfigProperty(name = "source.port", description = "The port to connect to on the remote host")
    Integer port;

    @ConfigProperty(name = "source.uri", description = "Remote target element")
    String uri;

    @ConfigProperty(name = "source.param.names", description = "Params name for http request separated by coma. Should has params value len")
    String paramNames;

    @ConfigProperty(name = "source.param.values", description = "Params value for http request separated by coma. Should has pramams names len")
    String paramValues;

    @ConfigProperty(name = "source.local", description = "Local source element identifcation", required = true)
    String local;

    public ExcelLoaderProcessor(Connector connector)
    {
        super(connector);
    }

    boolean loadFromClient()
    {
        if ("BASIC".equals(auth.toUpperCase()) || auth == null)
        {
            if ("HTTP".equals(protocol.toUpperCase()))
            {
                log.info("@loadFromClient: downloading content via HTTP");
            }
            else if ("FTP".equals(protocol.toUpperCase()))
            {
                log.info("@loadFromClient: downloading content via FTP");
            }
            else
            {
                log.error("@loadFromClient: unsoported protocol");
                return false;
            }

            BasicClientHandler<?> handler = "FTP".equals(protocol.toUpperCase())
                    ? new FTPDownload()
                    : new HTTPDownload();
            try
            {
                Optional<Map<String, String>> params = Optional.empty();
                if (this.paramNames != null)
                {
                    params = Optional.ofNullable(Functions.toMap(
                            paramNames == null ? new ArrayList<>()
                                    : Arrays.asList(paramNames.split(",")),
                            paramValues == null ? new ArrayList<>()
                                    : Arrays.asList(paramValues.split(","))));
                }
                handler.stage(hostname, Optional.ofNullable(port), user,
                        password, local, Optional.ofNullable(uri),
                        Optional.empty(), params);
                Boolean OK = handler.download();
                return OK;
            }
            catch (Exception e)
            {
                log.error(
                        "@loadFromClinet: problem dowloading content. Error: \n",
                        e.getMessage());
                return false;
            }
        }
        else
        {
            log.error("@loadFromClient: unsuported auth - {} - mechanism",
                    auth);
        }
        return false;
    }

    @Override
    protected Iterator<JsonNode> fullScan()
    {
        // saves temporal downloaded content in temporal folder
        if (loadFromClient())
        {
            Log.info(
                    "@fullScan: remote data correctly saved in local temporally file");
            ExcelMapping excelMapping = new ExcelMapping();
            List<Map<String, String>> data = excelMapping.mapping(local,
                    hasHeaders);

            // once data is loaded I remove temporally file
            Functions.deleteFile(local);

            List<JsonNode> scanned = new ArrayList<>();
            for (Map<String, String> dict : data)
            {
                Set<String> keys = dict.keySet();
                ObjectNode node = mapper.createObjectNode();
                for (String key : keys)
                {
                    node.put(key, dict.get(key));
                }
                scanned.add(node);
            }
            return scanned.iterator();
        }
        return Collections.emptyIterator();
    }
}