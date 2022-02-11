package org.bimrocket.ihub.dto;

/**
 * @author wilberquito
 */

public class DatasourceConfig
{
    String user;
    String pwd;
    String url;
    String driver;

    public DatasourceConfig(String user, String pwd, String url, String driver)
    {
        this.user = user;
        this.pwd = pwd;
        this.url = url;
        this.driver = driver;
    }

    public String getDriver()
    {
        return driver;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd)
    {
        this.pwd = pwd;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }
}
