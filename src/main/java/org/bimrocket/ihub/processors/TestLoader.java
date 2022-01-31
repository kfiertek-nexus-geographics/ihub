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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ProcessedObject;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.util.ConfigProperty;
import static org.bimrocket.ihub.connector.ProcessedObject.DELETE;
import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;
import static org.bimrocket.ihub.connector.ProcessedObject.UPDATE;

/**
 *
 * @author realor
 */
public class TestLoader extends Processor
{
  private final ObjectMapper mapper = new ObjectMapper();
  private final LinkedList<Entry> entries = new LinkedList<>();

  public TestLoader(Connector connector)
  {
    super(connector);
  }

  @ConfigProperty(description = "The returned object type", required = true)
  public String objectType;

  @ConfigProperty
  public List<String> objectFields = new ArrayList<>();

  @ConfigProperty
  public int objectSize;

  @ConfigProperty
  public Double objectPrice;

  @ConfigProperty
  public float offset;

  @ConfigProperty
  public boolean active;

  @ConfigProperty
  public Boolean active2;

  @Override
  public boolean processObject(ProcessedObject procObject)
  {
    if (entries.isEmpty()) return false;

    Entry entry = entries.poll();
    procObject.setOperation(entry.operation);
    procObject.setLocalId(entry.localId);
    procObject.setLocalObject(entry.localObject);
    procObject.setObjectType(objectType);
    return true;
  }

  public void createInsert(String localId)
  {
    ObjectNode object = mapper.createObjectNode();
    object.put("id", localId);
    object.put("name", "sample " + localId);
    object.put("date", System.currentTimeMillis());
    entries.offer(new Entry(INSERT, localId, object));
  }

  public void createUpdate(String localId)
  {
    ObjectNode object = mapper.createObjectNode();
    object.put("id", localId);
    object.put("name", "sample " + localId);
    object.put("date", System.currentTimeMillis());
    entries.offer(new Entry(UPDATE, localId, object));
  }

  public void createDelete(String localId)
  {
    entries.offer(new Entry(DELETE, localId, null));
  }

  class Entry
  {
    String operation;
    String localId;
    ObjectNode localObject;

    Entry(String operation, String localId, ObjectNode localObject)
    {
      this.operation = operation;
      this.localId = localId;
      this.localObject = localObject;
    }
  }
}
