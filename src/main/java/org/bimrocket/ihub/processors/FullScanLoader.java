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
package org.bimrocket.ihub.processors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bimrocket.ihub.connector.ProcessedObject.DELETE;
import static org.bimrocket.ihub.connector.ProcessedObject.UPDATE;
import org.bimrocket.ihub.connector.Processor;

/**
 *
 * @author realor
 */
public abstract class FullScanLoader extends Processor
{
  private static final Logger log = LoggerFactory
      .getLogger(FullScanLoader.class);
  
  private Iterator<JsonNode> updateIterator;
  private Iterator<IdPair> deleteIterator;
  private String phase = IDLE;
  private long updateStartTime;

  @ConfigProperty(name = "scanner.object.type", description = "The object type")
  public String objectType;

  @ConfigProperty(name = "scanner.interval.period", description = "Period to perform incoming records scan in seconds", required = false, defaultValue = "10 * 60")
  public int intervalPeriod = 10 * 60;

  public FullScanLoader(Connector connector)
  {
    super(connector);
  }

  @Override
  public void init()
  {
    phase = IDLE;
  }

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    switch (phase)
    {
    case IDLE:
      if (isTimeToScan())
      {
        log.debug("processObject@LoaderAbstractProcessor - Connector:{} it's time to scan, changing phase to UPDATE", this.getConnector().getName());
        phase = UPDATE;
        updateStartTime = System.currentTimeMillis();
        updateIterator = fullScan();
      }
      else
        return false;

    case UPDATE:
      if (loadUpdate(procObject))
      {
        log.debug("processObject@LoaderAbstractProcessor - Connector:{} loaded update object", this.getConnector().getName());     
        return true;
      }
      else
      {
        log.debug("processObject@LoaderAbstractProcessor - Connector:{} changing phase to DELETE", this.getConnector().getName());
        phase = DELETE;
        deleteIterator = purge();
      }

    case DELETE:
      if (loadDelete(procObject))
      {
        log.debug("processObject@LoaderAbstractProcessor - Connector:{} loaded delete object", this.getConnector().getName());
        return true;
      }
      else
      {
        log.debug("processObject@LoaderAbstractProcessor - Connector:{} changing phase to IDLE", this.getConnector().getName());
        phase = IDLE;
      }
      break;
    }
    return false;
  }

  @Override
  public void end()
  {
    phase = IDLE;
    updateIterator = null;
    deleteIterator = null;
  }

  protected boolean loadUpdate(ProcessedObject procObject)
  {
    if (updateIterator.hasNext())
    {
      procObject.setLocalObject(updateIterator.next());
      procObject.setObjectType(objectType);
      procObject.setOperation(UPDATE);
      return true;
    }
    return false;
  }

  protected boolean loadDelete(ProcessedObject procObject)
  {
    if (deleteIterator.hasNext())
    {
      IdPair idPair = deleteIterator.next();
      procObject.setLocalId(idPair.getLocalId());
      procObject.setLocalObject(this.mapper.nullNode());
      procObject.setObjectType(objectType);
      procObject.setOperation(DELETE);
      return true;
    }
    return false;
  }

  protected boolean isTimeToScan()
  {
    return (System.currentTimeMillis() - updateStartTime) > intervalPeriod
        * 1000;
  }

  protected Iterator<IdPair> purge()
  {
    Date lastUpdateDate = new Date(updateStartTime);
    IdPairRepository idPairRepository = connector.getConnectorService()
        .getIdPairRepository();

    List<IdPair> idPairs = idPairRepository
        .findByInventoryAndObjectTypeAndLastUpdateLessThan(
            connector.getInventory(), objectType, lastUpdateDate);

    return idPairs.iterator();
  }

  protected abstract Iterator<JsonNode> fullScan();

}
