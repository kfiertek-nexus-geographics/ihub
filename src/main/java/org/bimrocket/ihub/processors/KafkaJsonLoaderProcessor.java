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

import org.bimrocket.ihub.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class KafkaJsonLoaderProcessor extends KafkaLoaderAbstract
{
  private static final Logger log = LoggerFactory
      .getLogger(KafkaJsonLoaderProcessor.class);
  
  public KafkaJsonLoaderProcessor(Connector connector)
  {
    super(connector);
  }

  @Override
  public void init()
  {
    super.init();
  }

  @Override
  protected Iterator<JsonNode> fullScan()
  {
    log.debug("scan@KafkaJsonLoaderProcessor - Connector::{} scanning for objects from kafka to load", this.getConnector().getName());
    return super.getRecords().stream().map((consumerRecord) ->
    {
      try
      {
        log.debug("scan@KafkaJsonLoaderProcessor - Connector:{} found new record in kafka, record::{}", this.getConnector().getName(), consumerRecord);
        return this.mapper.readTree(consumerRecord);
      }
      catch (JsonProcessingException e)
      {
        log.error("scan@KafkaJsonLoaderProcessor - Connector::{} not a json object found in topic record::{}",this.getConnector().getName(), consumerRecord);
        return null;
      }
    }).iterator();
  }

  @Override
  public void end()
  {
    super.end();
    runnableKafka.shutdown();
    threatRunner.interrupt();
    runnableKafka = null;
    threatRunner = null;
  }

}
