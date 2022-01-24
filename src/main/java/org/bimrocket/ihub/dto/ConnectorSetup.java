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
package org.bimrocket.ihub.dto;

import org.springframework.data.annotation.Id;

/**
 *
 * @author realor
 */
public class ConnectorSetup
{
  @Id
  private String name;
  private String description;
  private String inventory;
  private Boolean autoStart;
  private Boolean singleRun;
  private Long waitMillis;

  private ComponentSetup loader;
  private ComponentSetup transformer;
  private ComponentSetup sender;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getInventory()
  {
    return inventory;
  }

  public void setInventory(String inventory)
  {
    this.inventory = inventory;
  }

  public Boolean getAutoStart()
  {
    return autoStart;
  }

  public void setAutoStart(Boolean autoStart)
  {
    this.autoStart = autoStart;
  }

  public Boolean getSingleRun()
  {
    return singleRun;
  }

  public void setSingleRun(Boolean singleRun)
  {
    this.singleRun = singleRun;
  }

  public Long getWaitMillis()
  {
    return waitMillis;
  }

  public void setWaitMillis(Long waitMillis)
  {
    this.waitMillis = waitMillis;
  }

  public ComponentSetup getLoader()
  {
    return loader;
  }

  public void setLoader(ComponentSetup loader)
  {
    this.loader = loader;
  }

  public ComponentSetup getTransformer()
  {
    return transformer;
  }

  public void setTransformer(ComponentSetup transformer)
  {
    this.transformer = transformer;
  }

  public ComponentSetup getSender()
  {
    return sender;
  }

  public void setSender(ComponentSetup sender)
  {
    this.sender = sender;
  }
}
