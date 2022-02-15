package org.bimrocket.ihub.util.download;

import org.bimrocket.ihub.interfaces.BasicClientHandler;
import org.python.jline.internal.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * 
 * @author wilberquito
 */
public class FTPDownload implements BasicClientHandler<FTPClient>
{

    /**
     * by default downloads binary files
     */
    @Override
    public FTPClient builder(String base, String user, String password,
            Map<String, Object> extras) throws Exception
    {
        FTPClient conn = new FTPClient();

        conn.addProtocolCommandListener(
                new PrintCommandListener(new PrintWriter(System.out)));

        Optional<Integer> fType = Optional
                .ofNullable((Integer) extras.get("filetype"));

        conn.connect(InetAddress.getByName(base));

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

        if (fType.isPresent())
        {
            conn.setFileType(fType.get());
        }
        else
        {
            conn.setFileType(FTP.BINARY_FILE_TYPE);
        }

        return conn;
    }

    @Override
    public boolean download(FTPClient client, String remote, String local)
    {
        try
        {
            FileOutputStream fos = new FileOutputStream(local);
            if (!client.retrieveFile(remote, fos))
                return false;
        }
        catch (Exception e)
        {
            Log.info("@download: problem creating local temporally file");
            return false;
        }
        return true;
    }

}
