package org.bimrocket.ihub.processors.excel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Optional;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.bimrocket.ihub.util.consumer.IConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelFtpConsumer implements IConsumer
{
    private static final Logger log = LoggerFactory
            .getLogger(ExcelFtpConsumer.class);

    InetAddress address;

    Optional<Integer> port;

    String uri;

    String username;

    String password;

    public ExcelFtpConsumer(InetAddress address, Optional<Integer> port,
            String uri, String username, String password)
    {
        this.address = address;
        this.port = port;
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    @Override
    public InputStream consum() throws Exception
    {
        FTPClient client = new FTPClient();

        client.addProtocolCommandListener(
                new PrintCommandListener(new PrintWriter(System.out)));

        if (port.isPresent())
        {
            client.connect(address, port.get());
        }
        else
        {
            client.connect(address);
        }

        if (!FTPReply.isPositiveCompletion(client.getReplyCode()))
        {
            client.disconnect();
            log.error("@consum: problem connecting to remote host");
            throw new IOException("@consum: problem connecting to remote host");
        }

        if (!client.login(username, password))
        {
            log.error("@consum: authentication failed");
            throw new IOException("@consum: authentication failed");
        }

        client.setFileType(FTP.BINARY_FILE_TYPE);

        // downloading...
        InputStream stream = client.retrieveFileStream(uri);

        if (client.isConnected())
        {
            client.logout();
            client.disconnect();
        }

        return stream;
    }

}
