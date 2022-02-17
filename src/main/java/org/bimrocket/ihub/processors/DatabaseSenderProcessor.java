package org.bimrocket.ihub.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 
 * @author wilberquito
 */
public class DatabaseSenderProcessor extends SenderAbstract
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

    @ConfigProperty(name = "db.query.check", description = "SQL query to know if object exists already in database")
    String queryCheck;

    @ConfigProperty(name = "db.query.update", description = "SQL query to update object")
    String queryUpdate;

    @ConfigProperty(name = "db.query.insert", description = "SQL query to insert object")
    String queryInsert;

    @ConfigProperty(name = "db.query.requires", description = "Required fields separated by semicolumn")
    String reqCols;

    @ConfigProperty(name = "db.query.optionals", description = "Optional fields separated by semicolumn")
    String optCols;

    @ConfigProperty(name = "db.request.timeout", description = "Timeout for uri request in seconds", defaultValue = "60")
    Integer timeout;

    NamedParameterJdbcTemplate jdbcTemplate;

    NamedParameterJdbcTemplate jdbcTemplateInstance()
    {
        if (jdbcTemplate == null)
        {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(url);
            hikariConfig.setDriverClassName(driver);
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        }
        return this.jdbcTemplate;
    }

    public DatabaseSenderProcessor(Connector connector)
    {
        super(connector);
    }

    @Override
    public boolean processObject(ProcessedObject procObject)
    {

        JsonNode send = this.getNodeToSend(procObject);

        if (send == null)
            return false;

        if (send.get("globalId") == null || send.get("element") == null)
            return false;

        MapSqlParameterSource sqlParameters = new MapSqlParameterSource();

        JsonNode element = send.get("element");
        List<String> requires = Arrays.asList(reqCols.split(","));
        List<String> optionals = Arrays.asList(optCols.split(","));

        for (String r : requires)
        {
            if (element.get(r) == null)
            {
                log.error(
                        "@processObject: required col does not appear in json node");
                return false;
            }
            else
            {
                sqlParameters.addValue(r, element.get(r).asText());
            }
        }

        for (String o : optionals)
        {
            if (element.get(o) == null)
            {
                log.info(
                        "@processObject: declared as optional - {} - and it is not present in json node",
                        o);
            }
            var item = element.get(o) == null ? null : element.get(o).asText();
            sqlParameters.addValue(o, item);
        }

        List<Map<String, Object>> resultSet = new ArrayList<>();

        try
        {
            resultSet = jdbcTemplateInstance().queryForList(queryCheck,
                    sqlParameters);
        }
        catch (Exception e)
        {
            log.error(
                    "@processObject: there was a problem consulting datasource, error:\n {}",
                    e.getMessage());
            return false;
        }

        if (resultSet.size() > 1)
        {
            log.error(
                    "@processObject: unexpected number of match, greater than 1");
            return false;
        }
        else
        {
            try
            {
                if (resultSet.size() == 1)
                {
                    log.info(
                            "@updateTable: updating register with global id - {}",
                            send.get("globalId"));
                    jdbcTemplateInstance().update(queryUpdate, sqlParameters);
                }
                else
                {
                    log.info("@updateTable: creating register in table");
                    jdbcTemplateInstance().update(queryInsert, sqlParameters);
                }
            }
            catch (Exception e)
            {
                log.error(
                        "@processObject: there was a problem modifing datasource, error:\n {}",
                        e.getMessage());
                return false;
            }

        }
        return true;
    }

}
