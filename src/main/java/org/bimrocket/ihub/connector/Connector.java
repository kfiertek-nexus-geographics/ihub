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
package org.bimrocket.ihub.connector;

import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import org.bimrocket.ihub.connector.loaders.NopLoader;
import org.bimrocket.ihub.connector.senders.NopSender;
import org.bimrocket.ihub.connector.transformers.NopTransformer;
import org.bimrocket.ihub.exceptions.InvalidConfigException;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.service.ConnectorService;
import static org.bimrocket.ihub.connector.ConnectorObject.DELETE;
import static org.bimrocket.ihub.connector.ConnectorObject.INSERT;
import static org.bimrocket.ihub.connector.ConnectorObject.UPDATE;

/**
 *
 * @author realor
 */
public class Connector implements Runnable
{
  public static final String RUNNING_STATUS = "RUNNING";
  public static final String STOPPED_STATUS = "STOPPED";
  public static final String STARTING_STATUS = "STARTING";
  public static final String STOPPING_STATUS = "STOPPING";

  protected ConnectorService service;

  protected String name;

  protected String description;

  protected String inventory;

  protected Loader loader;

  protected Transformer transformer;

  protected Sender sender;

  protected boolean end;

  protected long waitMillis = 1000;

  protected Thread thread;

  protected String status = STOPPED_STATUS;

  protected boolean debugEnabled = false;

  protected boolean singleRun = false;

  protected boolean autoStart = false;

  protected Date startTime;

  protected Date endTime;

  protected int processed;

  protected int ignored;

  protected int inserted;

  protected int updated;

  protected int deleted;

  protected Exception lastError;

  protected ArrayList<ConnectorObject> processedObjects = new ArrayList<>();

  private final ConnectorObject cObject = new ConnectorObject();

  public Connector(ConnectorService service, String name)
  {
    this.service = service;
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public String getDescription()
  {
    return description;
  }

  public Connector setDescription(String description)
  {
    this.description = description;
    return this;
  }

  public String getInventory()
  {
    return inventory;
  }

  public Connector setInventory(String inventory)
  {
    this.inventory = inventory;
    return this;
  }

  public ConnectorService getConnectorService()
  {
    return service;
  }

  public long getWaitMillis()
  {
    return waitMillis;
  }

  public Connector setWaitMillis(long waitMillis)
  {
    this.waitMillis = waitMillis;
    return this;
  }

  public Date getStartTime()
  {
    return startTime;
  }

  public Date getEndTime()
  {
    return endTime;
  }

  public int getProcessed()
  {
    return processed;
  }

  public int getIgnored()
  {
    return ignored;
  }

  public int getInserted()
  {
    return inserted;
  }

  public int getUpdated()
  {
    return updated;
  }

  public int getDeleted()
  {
    return deleted;
  }

  public Exception getLastError()
  {
    return lastError;
  }

  public Loader getLoader()
  {
    return loader;
  }

  public Loader createLoader(Class<Loader> cls)
    throws Exception
  {
    this.loader = createComponent(this, cls);
    return loader;
  }

  public Loader createLoader(String className) throws Exception
  {
    className = completeClassName(className, Loader.class, "loaders");
    this.loader = createComponent(this, className);
    return loader;
  }

  public Transformer getTransformer()
  {
    return transformer;
  }

  public Transformer createTransformer(Class<Transformer> cls)
    throws Exception
  {
    this.transformer = createComponent(this, cls);
    return transformer;
  }

  public Transformer createTransformer(String className)
    throws Exception
  {
    className = completeClassName(className, Transformer.class,
      "transformers");
    this.transformer = createComponent(this, className);
    return transformer;
  }

  public Sender getSender()
  {
    return sender;
  }

  public Sender createSender(Class<Sender> cls)
    throws Exception
  {
    this.sender = createComponent(this, cls);
    return sender;
  }

  public Sender createSender(String className) throws Exception
  {
    className = completeClassName(className, Sender.class, "senders");
    this.sender = createComponent(this, className);
    return sender;
  }

  public boolean isDebugEnabled()
  {
    return debugEnabled;
  }

  public Connector setDebugEnabled(boolean debugEnabled)
  {
    this.debugEnabled = debugEnabled;
    return this;
  }

  public boolean isSingleRun()
  {
    return singleRun;
  }

  public Connector setSingleRun(boolean singleRun)
  {
    this.singleRun = singleRun;
    return this;
  }

  public boolean isAutoStart()
  {
    return autoStart;
  }

  public Connector setAutoStart(boolean autoStart)
  {
    this.autoStart = autoStart;
    return this;
  }

  public String getStatus()
  {
    return status;
  }

  public ArrayList<ConnectorObject> getProcessedObjects()
  {
    return processedObjects;
  }

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    return buffer
      .append("Connector")
      .append("{ name: \"")
      .append(name)
      .append("\", description: \"")
      .append(description == null ? "" : description)
      .append("\", inventory: \"")
      .append(inventory)
      .append("\", status: ")
      .append(getStatus())
      .append(", autoStart: ")
      .append(autoStart)
      .append(", singleRun: ")
      .append(singleRun)
      .append(", debug: ")
      .append(debugEnabled)
      .append(", loader: ")
      .append(loader)
      .append(", transformer: ")
      .append(transformer)
      .append(", sender: ")
      .append(sender)
      .append(" }").toString();
  }

  @Override
  public void run()
  {
    status = RUNNING_STATUS;
    init();
    resetStatistics();

    while (!end)
    {
      try
      {
        cObject.reset();
        if (loader.loadObject(cObject))
        {
          transformer.transformObject(cObject);
          sender.sendObject(cObject); // insert, update or delete object

          updateIdPairRepository(cObject);
          updateStatistics(cObject);
          captureObject(cObject);
          // TODO: log processing
        }
        else
        {
          if (singleRun) end = true;
          else Thread.sleep(waitMillis);
        }
      }
      catch (InterruptedException ex)
      {
        // log
      }
      catch (Exception ex)
      {
        lastError = ex;
      }
    }
    end();
    thread = null;
    status = STOPPED_STATUS;
  }

  public synchronized Connector start()
  {
    if (thread == null)
    {
      thread = new Thread(this, "connector-" + name + ":" + inventory);
      thread.start();
      status = STARTING_STATUS;
    }
    return this;
  }

  public synchronized Connector stop()
  {
    if (thread != null)
    {
      end = true;
      thread.interrupt();
      status = STOPPING_STATUS;
    }
    return this;
  }

  public Connector save()
  {
    saveSetup();

    return this;
  }

  public ConnectorSetup saveSetup()
  {
    ConnectorSetup connSetup =
      service.getConnectorMapperService().getConnectorSetup(this);

    service.getConnectorSetupRepository().save(connSetup);

    return connSetup;
  }

  public Connector restore() throws Exception
  {
    restoreSetup();

    return this;
  }

  public ConnectorSetup restoreSetup() throws Exception
  {
    ConnectorSetup connSetup = null;

    Optional<ConnectorSetup> optConnSetup =
      service.getConnectorSetupRepository().findById(name);
    if (optConnSetup.isPresent())
    {
      connSetup = optConnSetup.get();
      service.getConnectorMapperService().setConnectorSetup(this, connSetup);
    }
    return connSetup;
  }

  protected void init()
  {
    end = false;

    startTime = new Date();

    if (loader == null) loader = new NopLoader(this);
    if (transformer == null) transformer = new NopTransformer(this);
    if (sender == null) sender = new NopSender(this);

    loader.init();
    transformer.init();
    sender.init();
  }

  public void end()
  {
    loader.end();
    transformer.end();
    sender.end();

    endTime = new Date();
  }

  void updateIdPairRepository(ConnectorObject cObject)
  {
    IdPairRepository idPairRepository = service.getIdPairRepository();
    if (cObject.isDelete())
    {
      idPairRepository.deleteByInventoryAndObjectTypeAndLocalId(
        inventory, cObject.getObjectType(), cObject.getLocalId());
    }
    else if (cObject.isInsert() || cObject.isUpdate())
    {
      IdPair idPair = new IdPair();
      idPair.setInventory(inventory);
      idPair.setObjectType(cObject.getObjectType());
      idPair.setLocalId(cObject.getLocalId());
      idPair.setGlobalId(cObject.getGlobalId());
      idPair.setLastUpdate(new Date());
      idPair.setConnectorName(name);
      idPairRepository.save(idPair);
    }
  }

  void resetStatistics()
  {
    processed = 0;
    ignored = 0;
    inserted = 0;
    updated = 0;
    deleted = 0;
  }

  void updateStatistics(ConnectorObject cObject)
  {
    processed++;
    switch (cObject.getOperation())
    {
      case INSERT: inserted++; break;
      case UPDATE: updated++; break;
      case DELETE: deleted++; break;
      default:
        ignored++;
    }
  }

  void captureObject(ConnectorObject cObject)
  {
    if (debugEnabled)
    {
      processedObjects.add(cObject.duplicate());
    }
  }

  <T extends Component> T createComponent(Connector connector,
    Class<T> componentClass) throws Exception
  {
    try
    {
      return componentClass.getConstructor(Connector.class)
        .newInstance(connector);
    }
    catch (Exception ex)
    {
      throw new InvalidConfigException(300,
        "Can not create component class {%s}", componentClass);
    }
  }

  <T extends Component> T createComponent(Connector connector,
    String className) throws InvalidConfigException
  {
    try
    {
      Class<T> componentClass = (Class<T>)Class.forName(className);
      return componentClass.getConstructor(Connector.class)
        .newInstance(connector);
    }
    catch (Exception ex)
    {
      throw new InvalidConfigException(300,
        "Can not create component class {%s}", className);
    }
  }

  String completeClassName(String className, Class baseClass, String pkg)
  {
    if (className.contains(".")) return className;

    String baseClassName = baseClass.getName();
    int index = baseClassName.lastIndexOf(".");
    return baseClassName.substring(0, index + 1) + pkg + "." + className;
  }
}
