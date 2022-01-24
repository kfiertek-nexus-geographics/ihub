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
package org.bimrocket.ihub.connector.transformers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.connector.ConnectorObject;
import org.bimrocket.ihub.connector.Transformer;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.util.ConfigProperty;
import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author realor
 */
public class RhinoTransformer extends Transformer
{
  @ConfigProperty(description="The script that makes the transformation")
  public String scriptCode = "";

  protected Script script;
  protected Context context;
  protected ScriptableObject scope;
  protected ObjectMapper mapper;

  public RhinoTransformer(Connector connector)
  {
    super(connector);
  }

  @Override
  public void init()
  {
    // init is called in connector thread
    context = Context.enter();
    script = context.compileString(scriptCode, "converter", 0, null);
    scope = context.initStandardObjects();
    mapper = new ObjectMapper();

    scope.put("connector", scope, connector);

    IdPairRepository idPairRepository =
      connector.getConnectorService().getIdPairRepository();
    scope.put("idPairRepository", scope, idPairRepository);
  }

  @Override
  public void transformObject(ConnectorObject cObject)
  {
    scope.put("localId", scope, cObject.getLocalId());
    scope.put("globalId", scope, cObject.getGlobalId());
    scope.put("objectType", scope, cObject.getObjectType());
    scope.put("operation", scope, cObject.getOperation());

    JsonNode localObject = cObject.getLocalObject();
    if (localObject != null)
    {
      scope.put("localObject", scope,
        new JsonNodeScriptable(scope, localObject));
    }
    JsonNode globalObject = cObject.getGlobalObject();
    if (globalObject != null)
    {
      scope.put("globalObject", scope,
        new JsonNodeScriptable(scope, globalObject));
    }
    script.exec(context, scope);

    cObject.setLocalId(Context.toString(scope.get("localId")));
    cObject.setGlobalId(Context.toString(scope.get("globalId")));
    cObject.setObjectType(Context.toString(scope.get("objectType")));
    cObject.setLocalObject(toJsonNode(scope.get("localObject")));
    cObject.setGlobalObject(toJsonNode(scope.get("globalObject")));
    cObject.setOperation(Context.toString(scope.get("operation")));
  }

  @Override
  public void end()
  {
    // end is called in connector thread
    Context.exit();
  }

  protected JsonNode toJsonNode(Object value)
  {
    if (value instanceof JsonNode)
    {
      return (JsonNode)value;
    }
    else if (value instanceof NativeJavaObject)
    {
      value = ((NativeJavaObject)value).unwrap();
      if (value instanceof JsonNode)
      {
        return (JsonNode) value;
      }
    }
    else if (value instanceof Scriptable)
    {
      return convertToJsonNode((Scriptable)value);
    }
    return null;
  }

  protected JsonNode convertToJsonNode(Scriptable scriptable)
  {
    JsonNode node = null;
    if (scriptable instanceof NativeObject)
    {
      ObjectNode objectNode = mapper.createObjectNode();
      NativeObject object = (NativeObject)scriptable;
      Object[] ids = object.getIds();
      for (Object id : ids)
      {
        String name = String.valueOf(id);
        Object value = object.get(id);
        setObjectNodeValue(objectNode, name, value);
      }
      node = objectNode;
    }
    else if (scriptable instanceof NativeArray)
    {
      ArrayNode arrayNode = mapper.createArrayNode();
      NativeArray array = (NativeArray)scriptable;
      for (Object elem : array)
      {
        addArrayNodeValue(arrayNode, elem);
      }
      node = arrayNode;
    }
    else if (scriptable instanceof JsonNodeScriptable)
    {
      JsonNodeScriptable nodeScriptable = (JsonNodeScriptable)scriptable;
      node = nodeScriptable.node;
    }
    return node;
  }

  private void setObjectNodeValue(ObjectNode node, String name, Object value)
  {
    if (value instanceof Scriptable)
    {
      Scriptable scriptValue = (Scriptable) value;
      node.set(name, convertToJsonNode(scriptValue));
    }
    else if (value instanceof JsonNode)
    {
      node.set(name, (JsonNode) value);
    }
    else if (value instanceof ConsString)
    {
      node.put(name, ((ConsString) value).toString());
    }
    else
    {
      if (value == null)
      {
        node.put(name, (String)null);
      }
      else if (value instanceof String)
      {
        node.put(name, (String)value);
      }
      else if (value instanceof Integer)
      {
        node.put(name, (Integer)value);
      }
      else if (value instanceof Long)
      {
        node.put(name, (Long)value);
      }
      else if (value instanceof Float)
      {
        node.put(name, (Float)value);
      }
      else if (value instanceof Double)
      {
        node.put(name, (Double)value);
      }
      else if (value instanceof Boolean)
      {
        node.put(name, (Boolean)value);
      }
    }
  }

  private void addArrayNodeValue(ArrayNode node, Object value)
  {
    if (value instanceof Scriptable)
    {
      Scriptable scriptValue = (Scriptable) value;
      node.add(convertToJsonNode(scriptValue));
    }
    else if (value instanceof JsonNode)
    {
      node.add((JsonNode) value);
    }
    else if (value instanceof ConsString)
    {
      node.add(((ConsString)value).toString());
    }
    else
    {
      if (value == null)
      {
        node.add((String) null);
      }
      else if (value instanceof String)
      {
        node.add((String)value);
      }
      else if (value instanceof Integer)
      {
        node.add((Integer)value);
      }
      else if (value instanceof Long)
      {
        node.add((Long)value);
      }
      else if (value instanceof Float)
      {
        node.add((Float)value);
      }
      else if (value instanceof Double)
      {
        node.add((Double)value);
      }
      else if (value instanceof Boolean)
      {
        node.add((Boolean)value);
      }
    }
  }

  public class JsonNodeScriptable extends NativeJavaObject
  {
    private final JsonNode node;

    public JsonNodeScriptable(Scriptable scope, JsonNode node)
    {
      super(scope, node, JsonNode.class);
      this.node = node;
    }

    @Override
    public String getClassName()
    {
      return "JsonNodeScriptable";
    }

    @Override
    public Object get(String name, Scriptable scope)
    {
      if (node.has(name))
      {
        JsonNode valueNode = node.get(name);
        if (valueNode.isArray() || valueNode.isObject())
        {
          return new JsonNodeScriptable(scope, valueNode);
        }
        else
        {
          return nodeToJava(valueNode);
        }
      }
      return super.get(name, scope);
    }

    @Override
    public Object get(int i, Scriptable scope)
    {
      if (i < node.size())
      {
        JsonNode valueNode = node.get(i);
        if (valueNode.isArray() || valueNode.isObject())
        {
          return new JsonNodeScriptable(scope, valueNode);
        }
        else
        {
          return nodeToJava(valueNode);
        }
      }
      return super.get(i, scope);
    }

    private Object nodeToJava(JsonNode valueNode)
    {
      if (valueNode.isInt())
      {
        return valueNode.asInt();
      }
      if (valueNode.isLong())
      {
        return valueNode.asInt();
      }
      if (valueNode.isNumber())
      {
        return valueNode.asDouble();
      }
      if (valueNode.isBoolean())
      {
        return valueNode.asBoolean();
      }
      if (valueNode.isTextual())
      {
        return valueNode.asText();
      }
      return null;
    }
  }
}
