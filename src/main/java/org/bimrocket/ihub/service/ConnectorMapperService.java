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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.connector.Loader;
import org.bimrocket.ihub.connector.Sender;
import org.bimrocket.ihub.connector.Transformer;
import org.bimrocket.ihub.dto.ProcessorSetup;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.ConnectorExecution;
import org.bimrocket.ihub.util.ConfigPropertyHandler;
import org.springframework.stereotype.Service;

/**
 *
 * @author realor
 */
@Service
public class ConnectorMapperService
{
  public ConnectorSetup getConnectorSetup(Connector connector)
  {
    ConnectorSetup connSetup = new ConnectorSetup();

    connSetup.setName(connector.getName());
    connSetup.setDescription(connector.getDescription());
    connSetup.setInventory(connector.getInventory());
    connSetup.setAutoStart(connector.isAutoStart());
    connSetup.setSingleRun(connector.isSingleRun());
    connSetup.setWaitMillis(connector.getWaitMillis());

    connSetup.setLoader(getProcessorSetup(connector.getLoader()));
    connSetup.setTransformer(getProcessorSetup(connector.getTransformer()));
    connSetup.setSender(getProcessorSetup(connector.getSender()));

    return connSetup;
  }

  public void setConnectorSetup(Connector connector, ConnectorSetup connSetup)
    throws Exception
  {
    if (connSetup.getDescription() != null)
    {
      connector.setDescription(connSetup.getDescription());
    }
    if (connSetup.getInventory() != null)
    {
      connector.setInventory(connSetup.getInventory());
    }
    if (connSetup.getAutoStart() != null)
    {
      connector.setAutoStart(connSetup.getAutoStart());
    }
    if (connSetup.getSingleRun() != null)
    {
      connector.setSingleRun(connSetup.getSingleRun());
    }
    if (connSetup.getWaitMillis() != null)
    {
      connector.setWaitMillis(connSetup.getWaitMillis());
    }

    ProcessorSetup loaderSetup = connSetup.getLoader();
    if (loaderSetup != null)
    {
      String className = loaderSetup.getClassName();
      Loader loader = className == null ?
        connector.getLoader() : connector.createLoader(className);
      setProcessorSetup(loader, loaderSetup);
    }
    ProcessorSetup transformerSetup = connSetup.getTransformer();
    if (transformerSetup != null)
    {
      String className = transformerSetup.getClassName();
      Transformer transformer = className == null ?
        connector.getTransformer() : connector.createTransformer(className);
      setProcessorSetup(transformer, transformerSetup);
    }
    ProcessorSetup senderSetup = connSetup.getSender();
    if (senderSetup != null)
    {
      String className = senderSetup.getClassName();
      Sender sender = className == null ?
        connector.getSender() : connector.createSender(className);
      setProcessorSetup(sender, senderSetup);
    }
  }

  public ProcessorSetup getProcessorSetup(Processor processor)
  {
    if (processor == null) return null;

    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setClassName(processor.getClass().getName());

    Map<String, Object> properties = new HashMap<>();

    Map<String, ConfigPropertyHandler> propHandlers =
      ConfigPropertyHandler.findProperties(processor.getClass());
    for (ConfigPropertyHandler propHandler : propHandlers.values())
    {
      String propertyName = propHandler.getName();
      try
      {
        properties.put(propertyName, propHandler.getValue(processor));
      }
      catch (Exception ex)
      {
        // log
      }
    }
    procSetup.setProperties(properties);

    return procSetup;
  }

  public void setProcessorSetup(Processor processor, ProcessorSetup procSetup)
  {
    if (processor == null) return;

    Map<String, Object> properties = procSetup.getProperties();
    if (properties == null) return;

    Map<String, ConfigPropertyHandler> propHandlers =
      ConfigPropertyHandler.findProperties(processor.getClass());
    for (Map.Entry<String, Object> entry : properties.entrySet())
    {
      String propertyName = entry.getKey();
      try
      {
        ConfigPropertyHandler propHandler = propHandlers.get(propertyName);
        if (propHandler != null)
        {
          propHandler.setValue(processor, properties.get(propertyName));
        }
      }
      catch (Exception ex)
      {
        // log
      }
    }
  }

  public ConnectorExecution getConnectorExecution(Connector connector)
  {
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

    ConnectorExecution execution = new ConnectorExecution();
    execution.setName(connector.getName());
    execution.setDescription(connector.getDescription());
    if (connector.getStartTime() != null)
    {
      execution.setStartTime(df.format(connector.getStartTime()));
    }
    if (connector.getEndTime() != null)
    {
      execution.setEndTime(df.format(connector.getEndTime()));
    }
    execution.setProcessed(connector.getProcessed());
    execution.setInserted(connector.getInserted());
    execution.setUpdated(connector.getUpdated());
    execution.setDeleted(connector.getDeleted());

    return execution;
  }
}
