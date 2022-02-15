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

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 
 * @author kfiertek-nexus-geographics
 *
 */
public abstract class SenderAbstract extends Processor
{

  private static final Logger log = LoggerFactory
      .getLogger(SenderAbstract.class);
  protected static final String GLOBAL_OBJECT = "global";
  protected static final String LOCAL_OBJECT = "local";

  @ConfigProperty(name = "sender.object.to.send", description = "Defines whenever send globalObject (already processed object probably with globalId defined) "
      + "or localObject (can be raw or processed) from incoming ProcessedObject Class, Allowed values: global or local", required = false, defaultValue = GLOBAL_OBJECT)
  public String objectToSend = GLOBAL_OBJECT;

  public SenderAbstract(Connector connector)
  {
    super(connector);
  }

  protected JsonNode getNodeToSend(ProcessedObject procObject)
  {
    if (objectToSend.equals(GLOBAL_OBJECT)
        && (procObject == null || procObject.getGlobalObject() == null
            || procObject.getGlobalObject().isNull()))
    {
      log.error("trying to send empty global object"
          + " with following ProcessedObject::{}", procObject.toString());
      return null;
    }
    else if (objectToSend.equals(LOCAL_OBJECT)
        && (procObject == null || procObject.getLocalObject() == null
            || procObject.getLocalObject().isNull()))
    {
      log.error("trying to send empty local object "
          + "with following ProcessedObject::{}", procObject.toString());
      return null;
    }

    return objectToSend.equals(GLOBAL_OBJECT) ? procObject.getGlobalObject()
        : procObject.getLocalObject();

  }

  @Override
  public void init()
  {
    super.init();
    switch (objectToSend)
    {
    case GLOBAL_OBJECT:
    case LOCAL_OBJECT:
      return;
    default:
      log.error(
          "config property (sender.object.to.send) is invalid. "
              + "Allowed values: global, local. Current value : {}",
          objectToSend);
      return;
    }
  }
}
