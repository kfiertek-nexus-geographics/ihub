
/**
 * 
 * @author wilberquito
 */
package org.bimrocket.ihub.processors;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.ExcelEnum;
import org.bimrocket.ihub.util.ExcelMapper;
import org.bimrocket.ihub.util.Functions;
import org.bimrocket.ihub.util.consumer.ConsumerBuilder;
import org.bimrocket.ihub.util.consumer.ConsumerEnum;
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

    @ConfigProperty(name = "source.has.headers", description = "First row should be treated as headers?")
    boolean hasHeaders = true;

    @ConfigProperty(name = "source.protocol", description = "Source protocol to rescue excel. Protocols supported:  HTTP | FTP")
    String protocol;

    @ConfigProperty(name = "source.username", description = "Source username ID")
    String username;

    @ConfigProperty(name = "source.password", description = "Source username password")
    String password;

    @ConfigProperty(name = "source.host", description = "IP or DNS", required = false)
    String host;

    @ConfigProperty(name = "source.port", description = "The port to connect to on the remote host", required = false)
    Integer port;

    @ConfigProperty(name = "source.uri", description = "Remote target element", required = false)
    String uri;

    @ConfigProperty(name = "source.query.names", description = "Params name for http request separated by coma. Should has params value len", required = false)
    String queryNames;

    @ConfigProperty(name = "source.query.values", description = "Params value for http request separated by coma. Should has pramams names len", required = false)
    String queryValues;

    @ConfigProperty(name = "source.url", description = "Complete path to target. Only supported for HTTP protocol", required = false)
    String addr;

    @ConfigProperty(name = "source.extension", description = "Downloaded source extension expected. Supported extensions: xlsx | xlx", required = true)
    String extension;

    public ExcelLoaderProcessor(Connector connector)
    {
        super(connector);
    }

    InputStream laodResponse() throws Exception
    {
        List<String> names = queryNames == null ? new ArrayList<>()
                : Functions.splitAndTrim(queryNames, ",");

        List<String> values = queryValues == null ? new ArrayList<>()
                : Functions.splitAndTrim(queryValues, ",");

        Map<String, String> queries = Functions.toMap(names, values);

        if ("FTP".equals(this.protocol.toUpperCase()))
        {
            InputStream stream = ConsumerBuilder.create(ConsumerEnum.EXCEL_FTP)
                    .base(this.host).port(this.port).uri(this.uri)
                    .queries(queries).username(this.username)
                    .password(this.password).build().consum();

            return stream;
        }
        else if ("HTTP".equals(this.protocol.toUpperCase()))
        {
            InputStream stream;
            if (this.addr != null)
            {
                stream = ConsumerBuilder.create(ConsumerEnum.EXCEL_HTTP)
                        .url(this.addr).username(this.username)
                        .password(this.password).build().consum();
            }
            else
            {
                stream = ConsumerBuilder.create(ConsumerEnum.EXCEL_HTTP)
                        .base(this.host).port(this.port).uri(this.uri)
                        .queries(queries).username(this.username)
                        .password(this.password).build().consum();
            }
            return stream;
        }
        throw new Exception("Unsupported protocol");
    }

    /**
     * maps remote input stream to iterator json array node
     * 
     * @param stream
     * @return
     * @throws Exception
     */
    Iterator<JsonNode> mapResponse(InputStream stream) throws Exception
    {
        ExcelEnum excelEnum = "xlsx".equals(this.extension.toLowerCase())
                || ".xlsx".equals(this.extension.toLowerCase()) ? ExcelEnum.XLSX
                        : ExcelEnum.XLS;

        File file = new File(
                excelEnum == ExcelEnum.XLS ? "excel-loader-result.xls"
                        : "excel-loader-result.xlsx");

        Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

        List<Map<String, String>> result = ExcelMapper.mapping(file, excelEnum,
                this.hasHeaders);

        List<JsonNode> scanned = new ArrayList<>();

        for (Map<String, String> dict : result)
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

    @Override
    protected Iterator<JsonNode> fullScan()
    {
        try
        {
            if (!"xlsx".equals(this.extension.toLowerCase())
                    && !".xlsx".equals(this.extension.toLowerCase())
                    && !"xls".equals(this.extension.toLowerCase())
                    && !".xls".equals(this.extension.toLowerCase()))
            {
                String err = String.format(
                        "Unsupported extension format '%s'. Supported extensions are: xlsx & xls",
                        this.extension);
                log.error(err);
                throw new Exception(err);
            }

            log.info("@fullScan: about to load excel file");
            InputStream stream = laodResponse();
            log.info("@fullScan: excel file correctly loaded");

            log.info("@fullScan: about to map excel data");
            Iterator<JsonNode> it = mapResponse(stream);
            log.info("@fullScan: excel data correctly mapped");

            log.info("@fullScan: data has just been loaded from '{}'",
                    this.addr == null ? this.host : this.addr);

            return it;
        }
        catch (Exception e)
        {
            log.error("Problem loading from - '{}'. Error:\n {}",
                    this.host != null ? this.host : this.addr, e.getMessage());
        }

        return Collections.emptyIterator();
    }
}
