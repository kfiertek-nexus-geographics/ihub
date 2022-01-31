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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.bimrocket.ihub.exceptions.InvalidSetupException;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.service.ConnectorService;
import static org.bimrocket.ihub.connector.ProcessedObject.DELETE;
import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;
import static org.bimrocket.ihub.connector.ProcessedObject.UPDATE;

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

  protected List<Processor> processors = new ArrayList<>();

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

  protected ArrayList<ProcessedObject> processedObjects = new ArrayList<>();

  private final ProcessedObject procObject = new ProcessedObject();

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

  public List<Processor> getProcessors()
  {
    return Collections.unmodifiableList(processors);
  }

  public int getProcessorCount()
  {
    return processors.size();
  }

  public <T extends Processor> T addProcessor(String className)
    throws InvalidSetupException
  {
    T processor = createProcessor(className);
    processors.add(processor);

    return processor;
  }

  public <T extends Processor> T addProcessor(Class<T> processorClass)
    throws InvalidSetupException
  {
    T processor = createProcessor(processorClass);
    processors.add(processor);

    return processor;
  }

  public <T extends Processor> T insertProcessor(String className, int index)
    throws InvalidSetupException
  {
    T processor = createProcessor(className);
    processors.add(index, processor);

    return processor;
  }

  public <T extends Processor> T insertProcessor(
    Class<T> processorClass, int index) throws InvalidSetupException
  {
    T processor = createProcessor(processorClass);
    processors.add(index, processor);

    return processor;
  }

  public <T extends Processor> T setProcessor(String className, int index)
    throws InvalidSetupException
  {
    T processor = createProcessor(className);
    if (thread != null) processor.end();
    processors.set(index, processor);

    return processor;
  }

  public <T extends Processor> T setProcessor(
    Class<T> processorClass, int index) throws InvalidSetupException
  {
    T processor = createProcessor(processorClass);
    if (thread != null) processor.end();
    processors.set(index, processor);

    return processor;
  }

  public void removeProcessor(int index)
  {
    Processor processor = processors.remove(index);
    if (thread != null) processor.end();
  }

  public void removeProcessor(Processor processor)
  {
    if (processors.remove(processor))
    {
      if (thread != null) processor.end();
    }
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

  public ArrayList<ProcessedObject> getProcessedObjects()
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
      .append(", processors: ")
      .append(processors)
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
        procObject.reset();

        boolean process = true;
        Iterator<Processor> iter = processors.iterator();
        while (iter.hasNext() && process)
        {
          Processor processor = iter.next();
          if (processor.isEnabled())
          {
            process = processor.processObject(procObject);
          }
        }
        if (!procObject.isIgnore())
        {
          updateIdPairRepository(procObject);
          updateStatistics(procObject);
          captureObject(procObject);
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
      service.getConnectorMapperService()
        .setConnectorSetup(this, connSetup, true);
    }
    return connSetup;
  }

  protected void init()
  {
    end = false;

    startTime = new Date();

    processors.forEach(processor ->
    {
      try
      {
        processor.init();
      }
      catch (Exception ex)
      {
        // log
      }
    });
  }

  public void end()
  {
    processors.forEach(processor ->
    {
      try
      {
        processor.end();
      }
      catch (Exception ex)
      {
        // log
      }
    });

    endTime = new Date();
  }

  void updateIdPairRepository(ProcessedObject procObject)
  {
    IdPairRepository idPairRepository = service.getIdPairRepository();

    idPairRepository.deleteByInventoryAndObjectTypeAndLocalId(inventory,
      procObject.getObjectType(), procObject.getLocalId());

    if (procObject.isInsert() || procObject.isUpdate())
    {
      IdPair idPair = new IdPair();
      idPair.setInventory(inventory);
      idPair.setObjectType(procObject.getObjectType());
      idPair.setLocalId(procObject.getLocalId());
      idPair.setGlobalId(procObject.getGlobalId());
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

  void updateStatistics(ProcessedObject procObject)
  {
    processed++;
    switch (procObject.getOperation())
    {
      case INSERT: inserted++; break;
      case UPDATE: updated++; break;
      case DELETE: deleted++; break;
      default:
        ignored++;
    }
  }

  void captureObject(ProcessedObject procObject)
  {
    if (debugEnabled)
    {
      processedObjects.add(procObject.duplicate());
    }
  }

  <T extends Processor> T createProcessor(Class<T> processorClass)
    throws InvalidSetupException
  {
    try
    {
      return processorClass.getConstructor(Connector.class).newInstance(this);
    }
    catch (Exception ex)
    {
      throw new InvalidSetupException(320,
        "Can not create processor class {%s}", processorClass);
    }
  }

  <T extends Processor> T createProcessor(String className)
    throws InvalidSetupException
  {
    try
    {
      className = completeClassName(className);
      Class<T> processorClass = (Class<T>)Class.forName(className);
      return createProcessor(processorClass);
    }
    catch (ClassNotFoundException ex)
    {
      throw new InvalidSetupException(330,
        "Invalid processor className {%s}", className);
    }
  }

  String completeClassName(String className)
  {
    if (className.contains(".")) return className;

    return "org.bimrocket.ihub.processors." + className;
  }
}
