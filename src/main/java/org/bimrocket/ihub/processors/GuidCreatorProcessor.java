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

import java.util.Iterator;
import java.util.List;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.dto.PostProcessorGlobalObject;
import org.bimrocket.ihub.util.ConfigProperty;
import org.bimrocket.ihub.util.InventoryUtils;
import org.python.icu.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Designed for objects that doesn't have globalId, usually after using one
 * loader which gets external data we only have localId, this processor adds to
 * ProcessedObject either new globalId or existing one from database IdPair's.
 * So use should be sequential after loader processor. Also sets global object
 * as json from dto PostProcessorGlobalObject
 * 
 * @author kfiertek-nexus-geographics
 *
 */
public class GuidCreatorProcessor extends Processor
{
  private static final Logger log = LoggerFactory
      .getLogger(GuidCreatorProcessor.class);

  private InventoryUtils invUtils;
  private List<IdPair> actualIdPairs;

  @ConfigProperty(name = "creator.object.type", description = "The object type to treat Global Ids")
  public String objectType;

  @ConfigProperty(name = "creator.object.path.local.id", description = "Path to local id in JsonNode object")
  public String pathLocalId;

  public GuidCreatorProcessor(Connector connector)
  {
    super(connector);
  }

  @Override
  public boolean processObject(ProcessedObject proObject)
  {
    if (proObject.isIgnore())
    {
      log.trace(
          "processObject@GuidCreatorProcessor - processed object is ignore");
      return true;
    }

    if ((proObject.getObjectType() == null
        || proObject.getObjectType().isBlank()))
    {

      log.error(
          "processObject@GuidCreatorProcessor - processed object type is null or blank");
      // This should never happen all loaders should assign object type to
      // ProcessedObject
      return false;
    }

    if (proObject.getLocalObject() == null
        && (proObject.isInsert() || proObject.isUpdate()))
    {
      log.error(
          "processObject@GuidCreatorProcessor - Connector::{}, processed object local object is null while operation is insert or update",
          this.getConnector().getName());
      // This should never happen all insert or update should have valid
      // JsonNode as localObject
      // In case of Ignore or Delete operation localObject can be null
      return false;
    }

    if ((proObject.getLocalId() == null || proObject.getLocalId().isBlank())
        && (proObject.isInsert() || proObject.isUpdate()))
    {
      log.debug(
          "processObject@GuidCreatorProcessor - Connector::{}, setting local id of ProcessedObject",
          this.getConnector().getName());
      JsonNode nodeToProcess = proObject.getLocalObject();
      String localId = null;

      String[] pathsLocalId = pathLocalId.split("\\.");
      JsonNode currentNode = nodeToProcess;
      for (int i = 0; i < pathsLocalId.length; i++)
      {
        currentNode = currentNode.get(pathsLocalId[i]);
      }
      localId = currentNode.asText();

      log.debug(
          "processObject@GuidCreatorProcessor - Connector::{}, local id found::{}",
          this.getConnector().getName(), localId);
      proObject.setLocalId(localId);
    }

    if (proObject.getGlobalId() == null || proObject.getGlobalId().isBlank())
    {
      log.debug(
          "processObject@GuidCreatorProcessor - Connector::{}, setting global id for ProcessedObject",
          this.getConnector().getName());
      var idPair = searchIdPair(proObject);
      if (idPair == null)
      {
        idPair = new IdPair();
        idPair.setConnectorName(connector.getName());
        idPair.setGlobalId(invUtils.getGuid());
        idPair.setInventory(connector.getInventory());
        idPair.setLocalId(proObject.getLocalId());
        idPair.setObjectType(proObject.getObjectType());
        idPair.setLastUpdate(Calendar.getInstance().getTime());
        actualIdPairs.add(idPair);
        proObject.setGlobalId(idPair.getGlobalId());
        proObject.setOperation(ProcessedObject.INSERT);
      }
      else
      {
        proObject.setGlobalId(idPair.getGlobalId());
        proObject.setOperation(ProcessedObject.INSERT);
      }
    }
    if (proObject.getGlobalObject() == null)
    {
      PostProcessorGlobalObject globObject = new PostProcessorGlobalObject()
          .globalId(proObject.getGlobalId());
      if (proObject.isDelete())
      {
        globObject.element(mapper.nullNode());
      }
      else
      {
        globObject.element(proObject.getLocalObject());
      }
      proObject.setGlobalObject(mapper.valueToTree(globObject));

    }
    return true;
  }

  @Override
  public void init()
  {
    super.init();
    this.invUtils = new InventoryUtils();
    this.setActualIdPairs();
  }

  private IdPair searchIdPair(ProcessedObject proObject)
  {
    IdPair found = null;
    Iterator<IdPair> ite = this.actualIdPairs.iterator();
    while (ite.hasNext() && found == null)
    {
      IdPair actual = ite.next();
      if (actual.getLocalId().equals(proObject.getLocalId()))
      {
        found = actual;
      }
    }
    return found;
  }

  private void setActualIdPairs()
  {
    this.actualIdPairs = this.getConnector().getConnectorService()
        .getIdPairRepository().findByInventoryAndObjectType(
            this.getConnector().getInventory(), this.objectType);
  }

}
