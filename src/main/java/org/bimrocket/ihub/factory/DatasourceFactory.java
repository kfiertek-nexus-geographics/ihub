package org.bimrocket.ihub.factory;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bimrocket.ihub.dto.DatasourceConfig;
import org.bimrocket.ihub.enums.DatasourceEnum;

/**
 * @author wilberquito
 */

public class DatasourceFactory
{

    public DataSource instance(DatasourceEnum source, DatasourceConfig config)
    {
        if (source == DatasourceEnum.HIKARI)
        {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getUrl());
            hikariConfig.setDriverClassName(config.getDriver());
            hikariConfig.setUsername(config.getUser());
            hikariConfig.setPassword(config.getPwd());
            HikariDataSource dataSource = new HikariDataSource(hikariConfig);
            return dataSource;
        }

        return null;
    }

}
