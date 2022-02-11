package org.bimrocket.ihub.processors;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.dto.DatasourceConfig;
import org.bimrocket.ihub.enums.DatasourceEnum;
import org.bimrocket.ihub.factory.DatasourceFactory;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author wilberquito
 */

public class DatabaseLoaderProcessor extends FullScanLoader
{

    private static final Logger log = LoggerFactory
            .getLogger(DatabaseLoaderProcessor.class);

    @ConfigProperty(name = "db.url", description = "Geoserver wfs url")
    String url;

    @ConfigProperty(name = "db.username", description = "User used for basic authentication")
    String username;

    @ConfigProperty(name = "db.password", description = "Password used for basic authentication")
    String password;

    @ConfigProperty(name = "db.auth", description = "Type of authentication currently only Basic is supported")
    String auth;

    @ConfigProperty(name = "db.driver", description = "Password used for basic authentication")
    String driver;

    @ConfigProperty(name = "db.query", description = "SQL query to rescue content from database")
    String query;

    @ConfigProperty(name = "db.request.timeout", description = "Timeout for uri request in seconds", defaultValue = "60")
    Integer timeout;

    JdbcTemplate jdbcTemplate;

    JdbcTemplate jdbcTemplateInstance()
    {
        if (jdbcTemplate == null)
        {
            DatasourceFactory factory = new DatasourceFactory();
            DataSource source = factory.instance(DatasourceEnum.HIKARI,
                    new DatasourceConfig(username, password, url, driver));
            jdbcTemplate = new JdbcTemplate(source);
        }
        return jdbcTemplate;
    }

    public DatabaseLoaderProcessor(Connector connector)
    {
        super(connector);
    }

    @Override
    protected Iterator<JsonNode> fullScan()
    {
        return loadResponse(timeout);
    }

    /**
     * Transforms rescued data through JdbcTemplate to JsonNode iterator
     * 
     * @param timeout
     * @return
     */
    Iterator<JsonNode> loadResponse(long timeout)
    {
        log.debug(
                "loadResponse@WfsLoaderProcessor - Connector::{}"
                        + " - init with timeout '{}'",
                this.connector.getName(), timeout);

        JdbcTemplate template = jdbcTemplateInstance();
        template.setQueryTimeout((int) timeout);

        List<JsonNode> response = new ArrayList<>();

        try
        {
            List<String> columns = new ArrayList<>();
            response = jdbcTemplate.query(query, (row, i) ->
            {
                if (i == 0)
                {
                    ResultSetMetaData metaData = row.getMetaData();
                    int count = metaData.getColumnCount();
                    for (int c = 1; c <= count; c++)
                    {
                        String col = metaData.getColumnName(c);
                        columns.add(col);
                    }
                }

                ObjectNode node = mapper.createObjectNode();

                for (String c : columns)
                    node.put(c, row.getString(c));

                return node;
            });
        }
        catch (DataAccessException e)
        {
            log.error(
                    "loadResponse@DatabaseLoader - {} - exception while sending petition : ",
                    this.connector.getName(), e);
            return Collections.emptyIterator();
        }

        return response.iterator();
    }

}
