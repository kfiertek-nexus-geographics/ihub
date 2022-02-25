package org.bimrocket.ihub.util.consumer;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import org.bimrocket.ihub.util.Functions;

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

    public IConsumer build() throws Exception
    {
        throw new Exception("Unsuported consum configuration");
    }
}
