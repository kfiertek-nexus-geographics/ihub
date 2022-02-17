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
package org.bimrocket.ihub.processors.kafka;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author kfiertek-nexus-geographics
 */
public class JsonKafkaSender extends KafkaSender
{
  private static final Logger log =
    LoggerFactory.getLogger(JsonKafkaSender.class);

  public JsonKafkaSender(Connector connector)
  {
    super(connector);
  }

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    JsonNode toSend = this.getNodeToSend(procObject);
    if (toSend == null)
    {
      return false;
    }

    try
    {
      var value = mapper.writeValueAsString(toSend);
      log.debug("sending {} json object to topic {}", toSend.toPrettyString(),
        this.topicName);
      this.template.send(this.topicName, value);
      return true;
    }
    catch (JsonProcessingException e)
    {
      log.error("error processing following json::{}, this should never happen",
        toSend.toPrettyString());
      return false;
    }
  }
}
