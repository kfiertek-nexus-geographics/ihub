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
import java.util.List;
import java.util.Optional;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.IdPair;
import org.bimrocket.ihub.repo.IdPairRepository;
import org.bimrocket.ihub.service.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.bimrocket.ihub.connector.ProcessedObject.DELETE;
import static org.bimrocket.ihub.connector.ProcessedObject.INSERT;
import static org.bimrocket.ihub.connector.ProcessedObject.UPDATE;

/**
 *
 * @author realor
 */
public class Connector implements Runnable
{
  private static final Logger log =
    LoggerFactory.getLogger(Connector.class);

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

  public synchronized int getProcessorCount()
  {
    return processors.size();
  }

  public synchronized List<Processor> getProcessors()
  {
    // avoid concurrent modification exceptions iterating over processor list

    return new ArrayList<>(processors);
  }

  public synchronized Connector setProcessors(List<Processor> processors)
  {
    // some changes will not take effect until connector restart

    processors.forEach(processor -> processor.setConnector(this));

    this.processors.clear();
    this.processors.addAll(processors);

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

  @Override
  public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    return buffer.append("Connector")
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
      .append(" }")
      .toString();
  }

  @Override
  public void run()
  {
    log.info("Connector {} started", name);
    startTime = new Date();
    status = RUNNING_STATUS;
    lastError = null;
    end = false;
    resetStatistics();

    List<Processor> runningProcessors = getProcessors();

    try
    {
      initProcessors(runningProcessors);
      log.debug("Entering loop");

      while (!end)
      {
        procObject.reset();

        int processorCount = 0;
        for (var processor : runningProcessors)
        {
          if (processor.isEnabled())
          {
            log.debug("Executing processor {}",
              processor.getClass().getName());

            if (processor.processObject(procObject))
            {
              processorCount++;
            }
            else break;
          }
        }

        if (!procObject.isIgnore())
        {
          updateIdPairRepository(procObject);
          updateStatistics(procObject);
          log.debug("Object processed, type: {}, operation: {}, "
            + "localId: {}, globalId: {}",
            procObject.getObjectType(), procObject.getOperation(),
            procObject.getLocalId(), procObject.getGlobalId());
        }

        if (processorCount == 0)
        {
          if (singleRun)
          {
            end = true;
          }
          else
          {
            synchronized (this)
            {
              if (!end)
              {
                wait(waitMillis);
              }
            }
          }
        }
      }
    }
    catch (Exception ex)
    {
      lastError = ex;
      log.error("An error has ocurred: {}", ex.toString());
    }
    finally
    {
      endProcessors(runningProcessors);
    }
    status = STOPPED_STATUS;
    endTime = new Date();
    log.info("Connector {} stopped", name);
    thread = null;
  }

  public synchronized Connector start()
  {
    if (thread == null)
    {
      thread = new Thread(this, "c:" + name);
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
      notify();
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
    log.info("Saving connector {}", name);

    ConnectorSetup connSetup = service.getConnectorMapperService()
      .getConnectorSetup(this);

    service.getConnectorSetupRepository().save(connSetup);

    return connSetup;
  }

  public Connector restore() throws Exception
  {
    log.info("Restoring connector {}", name);

    restoreSetup();

    return this;
  }

  public ConnectorSetup restoreSetup() throws Exception
  {
    ConnectorSetup connSetup = null;

    Optional<ConnectorSetup> optConnSetup = service
      .getConnectorSetupRepository().findById(name);
    if (optConnSetup.isPresent())
    {
      connSetup = optConnSetup.get();
      service.getConnectorMapperService().setConnectorSetup(this, connSetup,
        true);
    }
    return connSetup;
  }

  protected void initProcessors(List<Processor> processors)
    throws Exception
  {
    int initialized = 0;
    try
    {
      for (var processor : processors)
      {
        log.debug("Initializing processor #{}: {}", initialized,
          processor.getClass().getName());
        processor.init();
        initialized++;
      };
    }
    finally
    {
      // remove from list not initialized processors
      while (processors.size() > initialized)
      {
        processors.remove(processors.size() - 1); // remove last
      }
    }
  }

  public void endProcessors(List<Processor> processors)
  {
    int ended = 0;
    for (var processor : processors)
    {
      try
      {
        log.debug("Ending processor #{}: {}", ended,
          processor.getClass().getName());
        processor.end();
        ended++;
      }
      catch (Exception ex)
      {
        if (lastError == null) lastError = ex;
      }
    }
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
      case INSERT:
        inserted++;
        break;
      case UPDATE:
        updated++;
        break;
      case DELETE:
        deleted++;
        break;
      default:
        ignored++;
    }
  }
}
