/*
 * BIMROCKET
 *
 * Copyright (C) 2022, Ajuntament de Sant Feliu de Llobregat
 *
 * This program is licensed and may be used, modified and redistributed under
 * the terms of the European Public License (EUPL), either version 1.1 or (at
 * your option) any later version as soon as they are approved by the European
 * Commission.
 *
 * Alternatively, you may redistribute and/or modify this program under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either  version 3 of the License, or (at your option)
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the licenses for the specific language governing permissions, limitations
 * and more details.
 *
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along
 * with this program; if not, you may find them at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * http://www.gnu.org/licenses/
 * and
 * https://www.gnu.org/licenses/lgpl.txt
 */
package org.bimrocket.ihub.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.exceptions.InvalidNameException;
import org.bimrocket.ihub.exceptions.NotFoundException;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.bimrocket.ihub.repo.ConnectorSetupRepository;

/**
 *
 * @author realor
 */
@Service
public class ConnectorService
  implements ApplicationListener<ApplicationReadyEvent>
{
  private final Map<String, Connector> connectors =
    Collections.synchronizedMap(new HashMap<>());

  @Autowired
  IdPairRepository idPairRepository;

  @Autowired
  ConnectorSetupRepository connectorSetupRepository;

  @Autowired
  ConnectorMapperService connectorMapperService;

  private final Pattern connectorNamePattern =
    Pattern.compile("^([a-zA-Z_$][a-zA-Z\\d_$]*)$");

  public Connector createConnector(String connectorName)
    throws InvalidNameException
  {
    if (connectors.containsKey(connectorName))
      throw new InvalidNameException(310,
        "Connector name {%s} already exists", connectorName);

    if (!connectorNamePattern.matcher(connectorName).matches())
      throw new InvalidNameException(312,
        "Invalid connector name {%s}", connectorName);

    Connector connector = new Connector(this, connectorName);

    connectors.put(connector.getName(), connector);

    return connector;
  }

  public boolean destroyConnector(String connectorName, boolean deleteFromRepo)
  {
    Connector connector = connectors.remove(connectorName);
    if (connector != null)
    {
      connector.stop();

      if (deleteFromRepo) connectorSetupRepository.deleteById(connectorName);

      return true;
    }
    return false;
  }

  public boolean hasConnector(String connectorName)
  {
    return connectors.containsKey(connectorName);
  }

  public Connector getConnector(String connectorName)
    throws NotFoundException
  {
    Connector connector = connectors.get(connectorName);
    if (connector == null)
      throw new NotFoundException(150,
        "Connector {%s} not found", connectorName);
    return connector;
  }

  public ArrayList<Connector> getConnectors()
  {
    return new ArrayList<>(connectors.values());
  }

  public ArrayList<Connector> getConnectorsByName(String name)
  {
    ArrayList<Connector> filteredConnectors = new ArrayList<>();
    String lowerCaseName = name == null ? null : name.toLowerCase();
    connectors.values().forEach(connector ->
    {
      if (name == null
        || connector.getName().toLowerCase().contains(lowerCaseName))
      {
        filteredConnectors.add(connector);
      }
    });
    return filteredConnectors;
  }

  public IdPairRepository getIdPairRepository()
  {
    return idPairRepository;
  }

  public ConnectorSetupRepository getConnectorSetupRepository()
  {
    return connectorSetupRepository;
  }

  public ConnectorMapperService getConnectorMapperService()
  {
    return connectorMapperService;
  }

  public void restoreConnectors() throws Exception
  {
    connectorSetupRepository.findAll().forEach(connSetup ->
    {
      try
      {
        System.out.println("Restoring " + connSetup.getName());
        Connector connector = createConnector(connSetup.getName()).restore();
        if (connector.isAutoStart()) connector.start();
      }
      catch (Exception ex)
      {
        // log
      }
    });
  }

  @Override
	public void onApplicationEvent(ApplicationReadyEvent event)
  {
    System.out.println("INIT");
    System.out.println("idPairRepository: " + idPairRepository);
    System.out.println("ConnectorSetupRepository: " + connectorSetupRepository);

    try
    {
      restoreConnectors();
    }
    catch (Exception ex)
    {
      // log
    }
  }
}
