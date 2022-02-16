package org.bimrocket.ihub.util.download;

import org.python.jline.internal.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Optional;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * 
 * @author wilberquito
 */
public class FTPDownload extends AbstractBasicClient<FTPClient>
{
    private static final Logger log = LoggerFactory
            .getLogger(FTPDownload.class);

    Optional<String> uri = Optional.empty();
    String local = null;

    /**
     * by default downloads binary files
     */
    @Override
    public void stage(String hostname, Optional<Integer> port, String user,
            String password, String local, Optional<String> uri,
            Optional<Integer> responseType) throws Exception
    {
        FTPClient conn = new FTPClient();

        conn.addProtocolCommandListener(
                new PrintCommandListener(new PrintWriter(System.out)));

        if (port.isPresent())
        {
            conn.connect(InetAddress.getByName(hostname), port.get());
        }
        else
        {
            conn.connect(InetAddress.getByName(hostname));
        }

        if (!FTPReply.isPositiveCompletion(conn.getReplyCode()))
        {
            conn.disconnect();
            throw new IOException(
                    "@builder: problem connection to remote host");
        }

        if (!conn.login(user, password))
        {
            throw new IOException(
                    "@builder: authentication problem in remote host");
        }

        if (responseType.isPresent())
        {
            conn.setFileType(responseType.get());
        }
        else
        {
            conn.setFileType(FTP.BINARY_FILE_TYPE);
        }
        this.uri = uri;
        this.local = local;
        this.client = conn;
    }

    @Override
    public boolean download()
    {
        try
        {
            if (uri.isEmpty())
            {
                log.error("@download: this method needs uri");
                return false;
            }

            FileOutputStream fos = new FileOutputStream(local);
            if (!client.retrieveFile(uri.get(), fos))
                return false;

            reset();

        }
        catch (Exception e)
        {
            Log.info("@download: problem creating local temporally file");
            return false;
        }
        return true;
    }

    @Override
    protected void reset()
    {
        try
        {
            if (client.isConnected())
            {
                client.logout();
                client.disconnect();
            }
        }
        catch (Exception e)
        {
            Log.error("@reset: could not close connection correctly. Error: \n",
                    e.getMessage());
        }

        this.client = null;
        this.uri = Optional.empty();
        this.local = null;
    }

}
