package org.bimrocket.ihub.processors.sql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.processors.Sender;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 
 * @author wilberquito
 */
public class SQLSenderProcessor extends Sender
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

    @ConfigProperty(name = "sql.query.check", description = "SQL query to know if object exists already in database")
    String queryCheck;

    @ConfigProperty(name = "sql.query.update", description = "SQL query to update object")
    String queryUpdate;

    @ConfigProperty(name = "sql.query.insert", description = "SQL query to insert object", required = true)
    String queryInsert;

    @ConfigProperty(name = "sql.query.requires", description = "Required fields separated by semicolumn", required = true)
    String reqCols;

    @ConfigProperty(name = "sql.query.optionals", description = "Optional fields separated by semicolumn")
    String optCols;

    @ConfigProperty(name = "sql.timeout", description = "Timeout for uri request in seconds")
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

    @Override
    public boolean processObject(final ProcessedObject procObject)
    {
        return send(procObject);
    }

    boolean send(final ProcessedObject procObject)
    {
        JsonNode node = procObject.getLocalObject();

        if (node == null)
            return false;

        if (node.get("globalId") == null || node.get("element") == null)
            return false;

        MapSqlParameterSource sqlParameters = new MapSqlParameterSource();

        JsonNode element = node.get("element");
        List<String> requires = reqCols != null
                ? Arrays.asList(reqCols.split(","))
                : new ArrayList<>();
        List<String> optionals = optCols != null
                ? Arrays.asList(optCols.split(","))
                : new ArrayList<>();

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

        if (queryCheck != null)
        {
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
                            node.get("globalId"));
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
