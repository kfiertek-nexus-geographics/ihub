package org.bimrocket.ihub.processors.sql;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.bimrocket.ihub.dto.DatasourceConfig;
import org.bimrocket.ihub.enums.DatasourceEnum;
import org.bimrocket.ihub.factory.DatasourceFactory;
import org.bimrocket.ihub.processors.FullScanLoader;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author wilberquito
 */
public class SQLLoaderProcessor extends FullScanLoader
{

    private static final Logger log = LoggerFactory
            .getLogger(SQLLoaderProcessor.class);

    @ConfigProperty(name = "source.url", description = "Database url", required = true)
    String url;

    @ConfigProperty(name = "source.username", description = "User used for basic authentication", required = true)
    String username;

    @ConfigProperty(name = "source.password", description = "Password used for basic authentication", required = true)
    String password;

    @ConfigProperty(name = "source.driver", description = "Password used for basic authentication", required = true)
    String driver;

    @ConfigProperty(name = "source.localId", description = "Local identifier")
    String localId;

    @ConfigProperty(name = "sql.query", description = "SQL query to rescue content from database", required = true)
    String query;

    @ConfigProperty(name = "sql.timeout", description = "Timeout for database response query request")
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
    Iterator<JsonNode> loadResponse(final long timeout)
    {
        log.debug("loadResponse@SQLLoaderProcessor - Connector"
                + " - init with timeout '{}'", timeout);

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

                final ObjectMapper mapper = new ObjectMapper();
                ObjectNode node = mapper.createObjectNode();

                for (String c : columns)
                {
                    node.put(c, row.getString(c));
                }

                return node;
            });

            return response.iterator();
        }
        catch (DataAccessException e)
        {
            log.error(
                    "loadResponse@DatabaseLoader - could not load data correclty. Error:{}\n",
                    e.getMessage());
        }
        return Collections.emptyIterator();
    }

    @Override
    protected String getLocalId(JsonNode localObject)
    {
        return localObject.get(this.localId).asText();
    }

}
