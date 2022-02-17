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
package org.bimrocket.ihub.processors.test;

import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;

import java.util.List;

import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.util.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bimrocket.ihub.processors.Loader;

/**
 *
 * @author realor
 */
public class HardcodedLoader extends Loader
{
  private static final Logger log =
    LoggerFactory.getLogger(HardcodedLoader.class);

  private int id = 0;

  public HardcodedLoader(Connector connector)
  {
    super(connector);
  }

  @ConfigProperty(name = "hardcoded.loader.array.json.objects",
    description = "Sample json object array to load")
  public List<String> jsonObjects;

  @ConfigProperty(name = "hardcoded.loader.object.type",
    description = "Type of object we loading")
  public String objectType;

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (id != jsonObjects.size())
    {
      try
      {
        log.debug("setting local object of ProcessedObject to following::{}", jsonObjects.get(id));
        procObject.setLocalObject(mapper.readTree(jsonObjects.get(id)));
      }
      catch (JsonProcessingException e)
      {
        log.error("error reading json object with position::{} inside hardcoded array", id);
        return false;
      }
      procObject.setObjectType(objectType);
      procObject.setOperation(INSERT);
      id++;
      return true;
    }
    else
    {
      return false;
    }
  }

  @Override
  public void init()
  {
    this.id = 0;
  }
}
