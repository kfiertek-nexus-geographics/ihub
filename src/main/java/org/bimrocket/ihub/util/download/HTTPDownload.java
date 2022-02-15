package org.bimrocket.ihub.util.download;

import java.io.File;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
import org.python.jline.internal.Log;

/**
 * 
 * @author wilberquito
 */
public class HTTPDownload extends AbstractBasicClient<CloseableHttpClient>
{

    Optional<String> uri = Optional.empty();
    String local = null;
    String base = null;

    @Override
    public void stage(String base, String user, String password, String local,
            Optional<String> uri, Optional<Integer> responseType)
            throws Exception
    {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(user, password));

        client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider).build();
        this.uri = uri;
        this.local = local;
        this.base = base;
    }

    @Override
    public boolean download()
    {
        HttpGet request = new HttpGet(base);
        URIBuilder uri = new URIBuilder(request.getURI());

        if (this.uri.isPresent())
        {
            uri.addParameter("doc", this.uri.get());
        }

        try
        {
            CloseableHttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
                return false;

            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            Files.copy(is, new File(local).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            reset();
        }
        catch (Exception e)
        {
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
            Log.error("@reset: could not close connection correclty. Error: \n",
                    e.getMessage());
        }

        this.uri = Optional.empty();
        this.local = null;
        this.base = null;
    }
}
