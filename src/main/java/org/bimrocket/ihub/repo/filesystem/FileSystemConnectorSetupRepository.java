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
package org.bimrocket.ihub.repo.filesystem;

import java.util.Collections;
import java.util.Optional;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.bimrocket.ihub.repo.ConnectorSetupRepository;

/**
 *
 * @author realor
 */
@Repository
@ConditionalOnProperty(prefix = "data", name="store", havingValue="filesystem")
public class FileSystemConnectorSetupRepository
  implements ConnectorSetupRepository
{
  @Override
  public Iterable<ConnectorSetup> findAll()
  {
    return () ->
    {
      return Collections.emptyIterator();
    };
  }

  @Override
  public ConnectorSetup save(ConnectorSetup connSetup)
  {
    return connSetup;
  }

  @Override
  public Optional<ConnectorSetup> findById(String connectorName)
  {
    return Optional.empty();
  }

  @Override
  public void deleteById(String connectorName)
  {
  }
}
