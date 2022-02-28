package org.bimrocket.ihub.util.consumer;

import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;

import org.bimrocket.ihub.processors.excel.ExcelFtpConsumer;

/**
 * 
 * @author wilberquito
 */
public class ConsumerBuilder
{
    ConsumerEnum consumerEnum;

    Integer port;

    String uri;

    String base;

    String body;

    String username;

    String password;

    Map<String, String> queries;

    private ConsumerBuilder()
    {
    }

    public static ConsumerBuilder create(ConsumerEnum consumerEnum)
    {
        return new ConsumerBuilder().addConsumerEnum(consumerEnum);
    }

    private ConsumerBuilder addConsumerEnum(ConsumerEnum consumerEnum)
    {
        this.consumerEnum = consumerEnum;
        return this;
    }

    public ConsumerBuilder base(String base)
    {
        this.base = base;
        return this;
    }

    public ConsumerBuilder port(Integer port)
    {
        this.port = port;
        return this;
    }

    public ConsumerBuilder uri(String uri)
    {
        this.uri = uri;
        return this;
    }

    public ConsumerBuilder queries(Map<String, String> queries)
    {
        this.queries = queries;
        return this;
    }

    public ConsumerBuilder body(String body)
    {
        this.body = body;
        return this;
    }

    public ConsumerBuilder username(String username)
    {
        this.username = username;
        return this;
    }

    public ConsumerBuilder password(String password)
    {
        this.password = password;
        return this;
    }

    public IConsumer build() throws Exception
    {
        if (this.consumerEnum == ConsumerEnum.EXCEL_FTP)
        {
            return new ExcelFtpConsumer(InetAddress.getByName(this.base),
                    Optional.ofNullable(this.port), uri, username, password);
        }

        throw new Exception(String.format("Unsuported %s consum configuration",
                this.consumerEnum));
    }
}
