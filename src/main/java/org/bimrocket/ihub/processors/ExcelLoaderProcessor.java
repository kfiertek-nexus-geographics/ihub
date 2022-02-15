
/**
 * 
 * @author wilberquito
 */
package org.bimrocket.ihub.processors;

import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.net.ftp.FTPClient;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.interfaces.BasicClientHandler;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.download.FTPDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelLoaderProcessor extends FullScanLoader
{
    private static final Logger log = LoggerFactory
            .getLogger(ExcelLoaderProcessor.class);

    @ConfigProperty(name = "source.has.headers", description = "First row should be treated as headers?", required = true)
    boolean headers = true;

    @ConfigProperty(name = "source.protocol", description = "Source protocol to rescue excel { HTTP, FTP }", required = true)
    String protocol;

    @ConfigProperty(name = "source.auth", description = "Type of authentication currently only Basic is supported and it's taken as default")
    String auth;

    @ConfigProperty(name = "source.user", description = "Source user ID")
    String user;

    @ConfigProperty(name = "source.password", description = "Source user password")
    String password;

    @ConfigProperty(name = "source.base", description = "IP or DNS", required = true)
    String base;

    @ConfigProperty(name = "source.uri", description = "Remote target element", required = true)
    String uri;

    @ConfigProperty(name = "source.local", description = "Local source element identifcation. If it is not present file will use as name declaration uri")
    String local;

    public ExcelLoaderProcessor(Connector connector)
    {
        super(connector);
    }

    boolean loadFromClient()
    {
        if ("BASIC".equals(auth))
        {
            if ("FTP".equals(protocol))
            {
                BasicClientHandler<FTPClient> handler = new FTPDownload();
                try
                {
                    var client = handler.builder(base, user, password,
                            new HashMap<>());
                    Boolean OK = handler.download(client, uri, local);
                    if (client.isConnected())
                    {
                        client.logout();
                        client.disconnect();
                    }
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
            else if ("HTTP".equals(protocol))
            {

            }
            else
            {
                log.error("@loadFromClient: unsoported protocol");
            }
        }
        else
        {
            log.info("@loadFromClient: unsuported auth - {} - mechanism", auth);
        }
        return false;
    }

    @Override
    protected Iterator<JsonNode> fullScan()
    {
        return null;
    }
}