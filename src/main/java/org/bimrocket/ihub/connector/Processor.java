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

/**
 *
 * @author realor
 */
public abstract class Processor
{
  private Connector connector;
  protected String description;
  protected boolean enabled;

  public Processor()
  {
  }

  public final void setConnector(Connector connector)
  {
    if (this.connector != null)
      throw new RuntimeException("Connector already set for this Processor");

    this.connector = connector;
  }

  public final Connector getConnector()
  {
    return connector;
  }


  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public boolean isEnabled()
  {
    return enabled;
  }

  public void setEnabled(boolean enabled)
  {
    this.enabled = enabled;
  }

  public void init() throws Exception
  {
  }

  public abstract boolean processObject(ProcessedObject procObject);

  public void end()
  {
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "{}";
  }
}
