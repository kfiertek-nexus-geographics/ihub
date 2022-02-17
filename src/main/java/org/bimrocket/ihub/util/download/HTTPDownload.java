package org.bimrocket.ihub.util.download;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author wilberquito
 */
public class HTTPDownload extends AbstractBasicClient<CloseableHttpClient>
{
    private static final Logger log = LoggerFactory
            .getLogger(HTTPDownload.class);

    Optional<String> uri = Optional.empty();

    String hostname = null;

    String base = null;

    String local = null;

    Optional<Integer> port = Optional.empty();

    Optional<Map<String, String>> parameters = Optional.empty();

    @Override
    public void stage(String hostname, Optional<Integer> port, String user,
            String password, String local, Optional<String> uri,
            Optional<Integer> responseType,
            Optional<Map<String, String>> parameters) throws Exception
    {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));

        client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider).build();

        this.uri = uri;
        this.local = local;
        this.hostname = hostname;
        this.parameters = parameters;
        this.port = port;
    }

    /**
     * If you set the port in hostname please make sure to not define port
     * because it will be used here.
     * 
     * By default, if http and https was not defined in hostname http is setted
     * to be used in request
     */
    @Override
    public boolean download()
    {
        String point = hostname
                + (this.port.isEmpty() ? ""
                        : String.format(":%d", this.port.get()))
                + (this.uri.isEmpty() ? "" : this.uri.get());

        if (!point.startsWith("http://") && !point.startsWith("https://"))
        {
            point = "http://".concat(point);
        }

        try
        {
            URIBuilder uriBuilder = new URIBuilder(point);
            if (this.parameters.isPresent())
            {
                Map<String, String> toAdd = this.parameters.get();
                for (var entry : toAdd.entrySet())
                {
                    uriBuilder.setParameter(entry.getKey(), entry.getValue());
                }
            }

            HttpGet request = new HttpGet(uriBuilder.build());

            CloseableHttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
            {
                log.error(
                        "@download: remote server did not response with OK status");
                return false;
            }

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            Files.copy(is, new File(local).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            reset();
        }
        catch (Exception e)
        {
            log.error(
                    "@download: there was a problem downloading remote excel file. Error:\n {}",
                    e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void reset()
    {
        try
        {
            client.close();
        }
        catch (Exception e)
        {
            log.error("@reset: could not close connection correclty. Error: \n",
                    e.getMessage());
        }
        this.uri = Optional.empty();
        this.local = null;
        this.hostname = null;
        this.port = Optional.empty();
    }
}
