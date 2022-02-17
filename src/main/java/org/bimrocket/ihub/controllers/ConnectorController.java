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
package org.bimrocket.ihub.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.dto.ConnectorExecution;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.ProcessorType;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.bimrocket.ihub.service.ConnectorMapperService;
import org.bimrocket.ihub.service.ConnectorService;
import org.bimrocket.ihub.service.ProcessorTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author realor
 */
@RestController
public class ConnectorController
{
  @Autowired
  ConnectorService connectorService;

  @Autowired
  ConnectorMapperService connectorMapperService;

  @Autowired
  ProcessorTypeService processorTypeService;

  @GetMapping(path = "/connectors", produces = "application/json")
  public List<ConnectorExecution> getConnectors()
  {
    List<ConnectorExecution> response = new ArrayList<>();
    ArrayList<Connector> connectors = connectorService.getConnectors();
    for (Connector connector : connectors)
    {
      response.add(connectorMapperService.getConnectorExecution(connector));
    }
    return response;
  }

  @GetMapping(path = "/connectors/{connectorName}",
    produces = "application/json")
  public ConnectorSetup getConnector(@PathVariable String connectorName)
      throws Exception
  {
    Connector connector = connectorService.getConnector(connectorName);
    return connectorMapperService.getConnectorSetup(connector);
  }

  @PostMapping(path = "/connectors", produces = "application/json")
  public ConnectorSetup createConnector(@RequestBody ConnectorSetup connSetup)
      throws Exception
  {
    String name = connSetup.getName();
    if (name == null)
      throw new InvalidSetupException(300, "Connection name is null");

    Connector connector = connectorService.createConnector(name);
    connectorMapperService.setConnectorSetup(connector, connSetup);

    connSetup = connector.saveSetup();

    if (connector.isAutoStart())
      connector.start();

    return connSetup;
  }

  @PutMapping(path = "/connectors/{connectorName}",
    produces = "application/json")
  public ConnectorSetup updateConnector(@PathVariable String connectorName,
      @RequestBody ConnectorSetup connSetup) throws Exception
  {
    Connector connector = connectorService.getConnector(connectorName);
    connectorMapperService.setConnectorSetup(connector, connSetup);

    connSetup = connector.saveSetup();

    return connSetup;
  }

  @DeleteMapping(path = "/connectors/{connectorName}",
    produces = "application/json")
  public boolean destroyConnector(@PathVariable String connectorName)
      throws Exception
  {
    return connectorService.destroyConnector(connectorName, true);
  }

  @GetMapping(path = "/connectors/{connectorName}/status",
    produces = "application/json")
  public ConnectorExecution getConnectorStatus(
      @PathVariable String connectorName) throws Exception
  {
    Connector connector = connectorService.getConnector(connectorName);
    return connectorMapperService.getConnectorExecution(connector);
  }

  @GetMapping(path = "/connectors/{connectorName}/start",
    produces = "application/json")
  public ConnectorExecution startConnector(@PathVariable String connectorName)
      throws Exception
  {
    Connector connector = connectorService.getConnector(connectorName);
    connector.start();
    return connectorMapperService.getConnectorExecution(connector);
  }

  @GetMapping(path = "/connectors/{connectorName}/stop",
    produces = "application/json")
  public ConnectorExecution stopConnector(@PathVariable String connectorName)
      throws Exception
  {
    Connector connector = connectorService.getConnector(connectorName);
    connector.stop();
    return connectorMapperService.getConnectorExecution(connector);
  }

  @GetMapping(path = "/connectors/{connectorName}/executions",
    produces = "application/json")
  public List<ConnectorExecution> getConnectorExecutions(
      @PathVariable String connectorName) throws Exception
  {
    return Collections.EMPTY_LIST;
  }

  @GetMapping(path = "/processors", produces = "application/json")
  public List<ProcessorType> getProcessors(
      @RequestParam(name = "name", required = false) String className)
      throws Exception
  {
    return processorTypeService.findProcessorTypes(className);
  }
}
