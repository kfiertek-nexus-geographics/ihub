/**
BIMROCKET

Copyright (C) 2022, CONSULTORIA TECNICA NEXUS GEOGRAPHICS

This program is licensed and may be used, modified and redistributed under
the terms of the European Public License (EUPL), either version 1.1 or (at
your option) any later version as soon as they are approved by the European
Commission.

Alternatively, you may redistribute and/or modify this program under the
terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either  version 3 of the License, or (at your option)
any later version.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the licenses for the specific language governing permissions, limitations
and more details.

You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
with this program; if not, you may find them at:

https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
http://www.gnu.org/licenses/
and
https://www.gnu.org/licenses/lgpl.txt
**/
package org.bimrocket.ihub.processors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.util.ConfigProperty;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * 
 * @author kfiertek-nexus-geographics
 *
 */
public class GemwebLoaderProcessor extends FullScanLoader
{
  private static final Logger log = LoggerFactory
      .getLogger(GemwebLoaderProcessor.class);

  private static final String AUTH_BEARER = "Bearer";

  @ConfigProperty(name = "gemweb.url", description = "Gemweb url")
  private String url;

  @ConfigProperty(name = "gemweb.client.id", description = "Used to obtain token")
  private String clientId;

  @ConfigProperty(name = "gemweb.client.secret", description = "Secret used for authentication")
  private String clientSecret;

  @ConfigProperty(name = "gemweb.category", description = "Category to load from gemweb")
  private String category;

  @ConfigProperty(name = "gemweb.auth", description = "Authentication used currently supported only Bearer", required = false, defaultValue = "Bearer")
  private String auth = AUTH_BEARER;

  @ConfigProperty(name = "gemweb.request.timeout", description = "Timeout for uri request in seconds", defaultValue = "60")
  private Integer timeoutS;

  @ConfigProperty(name = "gemweb.records.path", description = "Path to records inside json response")
  private String recordsPath;

  public GemwebLoaderProcessor(Connector connector)
  {
    super(connector);
  }

  @Override
  protected Iterator<JsonNode> fullScan()
  {
    return loadResponse(timeoutS);
  }

  private HttpClient buildHttpClient()
  {
    int timeoutMs = timeoutS * 1000;
    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder.setConnectTimeout(timeoutMs);
    requestBuilder.setConnectionRequestTimeout(timeoutMs);

    HttpClientBuilder clientBuilder = HttpClients.custom();
    clientBuilder.setDefaultRequestConfig(requestBuilder.build());
    return clientBuilder.build();
  }

  private String getAccessToken()
  {

    HttpPost httpPost = new HttpPost(url);

    List<NameValuePair> nvps = new ArrayList<NameValuePair>();
    nvps.add(new BasicNameValuePair("request", "get_token"));
    nvps.add(new BasicNameValuePair("client_id", clientId));
    nvps.add(new BasicNameValuePair("client_secret", clientSecret));
    nvps.add(new BasicNameValuePair("grant_type", "client_credentials"));
    try
    {
      httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      httpPost.setHeader("Accept", "application/xml");
      httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

      HttpResponse resp = buildHttpClient().execute(httpPost);

      String ret = inputStreamResponseToString(resp.getEntity().getContent());

      if (ret == null)
      {
        log.error("response entity stream is null or empty");
        return null;
      }
      XmlMapper xmlMapper = new XmlMapper();
      JsonNode node;

      node = xmlMapper.readTree(ret.getBytes(StandardCharsets.UTF_8));

      String accessToken = "";

      log.trace("response '{}'", ret);

      if (node.get("error") != null)
      {
        log.error("error in authentication, error responded : {}",
            node.get("resultat").get("error"));
      }
      JsonNode resultat = node.get("access_token");
      if (resultat != null)
      {
        accessToken = resultat.asText();
      }
      return accessToken;
    }
    catch (IOException e)
    {
      log.error("exception in getting accessToken", e);
      return null;
    }
  }

  private String inputStreamResponseToString(InputStream reader)
  {
    StringBuilder sb = new StringBuilder();

    log.debug("reading response of petition stream");
    try (InputStreamReader inputStreamReader = new InputStreamReader(reader))
    {
      BufferedReader br = new BufferedReader(inputStreamReader);
      String readLine;
      while (((readLine = br.readLine()) != null))
      {
        sb.append("\n").append(readLine);
      }
    }
    catch (IOException e)
    {
      log.error("Error reading response", e);
      return null;
    }

    String ret = sb.toString().replaceAll("\r\n", "").replaceAll("\t", "")
        .replaceAll("\n", "");
    return ret;
  }

  private String getAuthHeader(String accessToken)
  {
    String authHeader = "";
    if (auth != null && auth.equals(AUTH_BEARER))
    {
      authHeader = AUTH_BEARER + " " + accessToken;
    }
    return authHeader;
  }

  private Iterator<JsonNode> loadResponse(long timeout)
  {

    log.debug("init with timeout '{}'", timeout);

    try
    {
      log.debug("execute httpClient built");
      var httpPost = new HttpPost(url);
      String accessToken = this.getAccessToken();
      if (accessToken == null || accessToken.isBlank())
      {
        log.error("access token is empty {} returning empty iterator",
            accessToken);
        return Collections.emptyIterator();
      }
      String authHeader = this.getAuthHeader(accessToken);
      List<NameValuePair> nvps = new ArrayList<NameValuePair>();
      nvps.add(new BasicNameValuePair("request", "get_inventory"));
      nvps.add(new BasicNameValuePair("access_token", accessToken));
      nvps.add(new BasicNameValuePair("category", category));

      httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
      httpPost.setHeader("Accept", "application/xml");
      httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
      httpPost.setHeader("Authorization", authHeader);
      HttpResponse resp = buildHttpClient().execute(httpPost);

      String ret = inputStreamResponseToString(resp.getEntity().getContent());
      String json = XML.toJSONObject(ret).toString();

      try
      {
        String[] recordsPathSep = recordsPath.split("\\.");
        JsonNode currentNode = mapper.readTree(json);
        for (int i = 0; i < recordsPathSep.length; i++)
        {
          currentNode = currentNode.get(recordsPathSep[i]);
        }
        JsonNode all = currentNode;
        if (all.isArray())
        {
          return ((ArrayNode) all).iterator();
        }
        else
        {
          log.error("all is not a ArrayNode, all::{}",
              mapper.writeValueAsString(all));
          return Collections.emptyIterator();
        }
      }
      catch (Exception e)
      {
        log.error("exception while sending petition : ", e);

      }
      return Collections.emptyIterator();

    }
    catch (ConnectTimeoutException | SocketTimeoutException e)
    {
      log.error("timeout while sending petition : ", e);
      return Collections.emptyIterator();
    }
    catch (Exception e)
    {
      log.error("exception while sending petition : ", e);
      return Collections.emptyIterator();
    }
  }

}
