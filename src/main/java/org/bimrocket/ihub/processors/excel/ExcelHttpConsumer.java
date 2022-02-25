package org.bimrocket.ihub.processors.excel;

import java.io.InputStream;
import java.net.URI;

import com.google.common.net.HttpHeaders;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bimrocket.ihub.util.consumer.IConsumer;
import org.springframework.http.MediaType;

public class ExcelHttpConsumer implements IConsumer
{
    URI request;

    String username;

    String password;

    public ExcelHttpConsumer(URI request, String username, String password)
    {
        this.request = request;
        this.username = username;
        this.password = password;
    }

    @Override
    public InputStream consum() throws Exception
    {
        URIBuilder uriBuilder = new URIBuilder(request);

        HttpGet get = new HttpGet(uriBuilder.build());

        get.setHeader(HttpHeaders.ACCEPT,
                MediaType.EXCE + ", " + MediaType.APPLICATION_XML_VALUE);

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider).build();

        CloseableHttpResponse response = client.execute(get);

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
        {
            String err = String.format(
                    "Post request does not succed for request '%s'", request);
            throw new Exception(err);
        }

        HttpEntity entity = response.getEntity();

        response.close();

        InputStream stream = entity.getContent();

        return stream;
    }
}
