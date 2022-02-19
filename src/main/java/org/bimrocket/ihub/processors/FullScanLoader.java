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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.util.ConfigProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author realor
 */
public abstract class FullScanLoader extends Loader
{
  private static final Logger log =
    LoggerFactory.getLogger(FullScanLoader.class);

  @ConfigProperty(name = "intervalPeriod",
    description = "Period to perform incoming records scan in seconds")
  public int intervalPeriod = 10 * 60;

  private static final String IDLE = "idle";
  private static final String UPDATE = "update";
  private static final String DELETE = "delete";

  private Iterator<JsonNode> updateIterator;
  private Iterator<IdPair> deleteIterator;
  private String phase = IDLE;
  private long updateStartTime;

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
          log.debug("it's time to scan, changing phase to UPDATE");
          phase = UPDATE;
          updateStartTime = System.currentTimeMillis();
          updateIterator = fullScan();
        }
        else
        {
          return false;
        }

      case UPDATE:
        if (loadUpdate(procObject))
        {
          log.debug("loaded update object");
          return true;
        }
        else
        {
          log.debug("changing phase to DELETE");
          phase = DELETE;
          deleteIterator = purge();
        }

      case DELETE:
        if (loadDelete(procObject))
        {
          log.debug("loaded delete object");
          return true;
        }
        else
        {
          log.debug("changing phase to IDLE");
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
      procObject.setLocalObject(null);
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

    IdPairRepository idPairRepository =
      getConnector().getConnectorService().getIdPairRepository();

    List<IdPair> idPairs = idPairRepository
      .findByInventoryAndObjectTypeAndLastUpdateLessThan(
        getConnector().getInventory(), objectType, lastUpdateDate);

    return idPairs.iterator();
  }

  protected abstract Iterator<JsonNode> fullScan();

}
