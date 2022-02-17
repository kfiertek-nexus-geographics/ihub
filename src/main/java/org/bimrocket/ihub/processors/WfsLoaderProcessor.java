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

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * 
 * @author kfiertek-nexus-geographics
 *
 */
public class WfsLoaderProcessor extends FullScanLoader
{
  private static final Logger log = LoggerFactory
      .getLogger(WfsLoaderProcessor.class);

  private final String AUTH_BASIC = "Basic";

  @ConfigProperty(name = "wfs.url", description = "Geoserver wfs url")
  String url;

  @ConfigProperty(name = "wfs.params", description = "Geoserver url query params")
  String urlParams;

  @ConfigProperty(name = "wfs.username", description = "User used for basic authentication")
  String username;

  @ConfigProperty(name = "wfs.password", description = "Password used for basic authentication")
  String password;

  @ConfigProperty(name = "wfs.layers", description = "Layers to load from geoserver can be multiple comma separeted like this: layers.01, layers.02...")
  String layersLoad;

  @ConfigProperty(name = "wfs.format", description = "Format of petition's body sent to geoserver")
  String formatPetition;

  @ConfigProperty(name = "wfs.auth", description = "Type of authentication currently only Basic is supported")
  String auth;

  @ConfigProperty(name = "wfs.request.timeout", description = "Timeout for uri request in seconds", defaultValue = "60")
  Integer timeoutS;

  public WfsLoaderProcessor(Connector connector)
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

  private HttpUriRequest buildRequest()
  {

    RequestBuilder request = RequestBuilder.get()
        .setUri(this.url + this.urlParams + "&typeName=" + layersLoad
            + "&outputFormat=" + this.formatPetition);
    if (auth != null && auth.equals(AUTH_BASIC))
    {
      request.setHeader(HttpHeaders.AUTHORIZATION, getAuthHeader());
    }

    return request.build();
  }

  private String getAuthHeader()
  {
    String authHeader = "";
    if (auth != null && auth.equals(AUTH_BASIC))
    {
      String auth = username + ":" + password;
      byte[] encodedAuth = Base64
          .encodeBase64(auth.getBytes(StandardCharsets.ISO_8859_1));
      authHeader = AUTH_BASIC + " " + new String(encodedAuth);
    }
    return authHeader;
  }

  private Iterator<JsonNode> loadResponse(long timeout)
  {

    log.debug("init with timeout '{}'", timeout);

    JsonNode jsonResponse;

    try
    {
      log.debug("execute httpClient built");
      HttpResponse response = buildHttpClient().execute(buildRequest());
      String bodyResp = EntityUtils.toString(response.getEntity(),
          StandardCharsets.UTF_8);
      jsonResponse = mapper.readTree(bodyResp);

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

    try
    {
      ArrayNode all = mapper.valueToTree(jsonResponse.get("features"));

      return all.iterator();
    }
    catch (Exception e)
    {
      log.error("exception while parsing features : ", e);
      return Collections.emptyIterator();
    }

  }

}
