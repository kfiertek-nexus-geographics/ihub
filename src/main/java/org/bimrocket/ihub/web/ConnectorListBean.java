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
package org.bimrocket.ihub.web;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.faces.bean.ViewScoped;
import javax.faces.event.FacesEvent;
import org.bimrocket.ihub.connector.Connector;
import org.bimrocket.ihub.dto.ConnectorSetup;
import org.bimrocket.ihub.dto.ProcessorProperty;
import org.bimrocket.ihub.dto.ProcessorSetup;
import org.bimrocket.ihub.dto.ProcessorType;
import org.bimrocket.ihub.exceptions.NotFoundException;
import org.bimrocket.ihub.service.ConnectorMapperService;
import org.bimrocket.ihub.service.ConnectorService;
import org.bimrocket.ihub.service.ProcessorService;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import static org.bimrocket.ihub.connector.Connector.RUNNING_STATUS;
import static org.bimrocket.ihub.connector.Connector.STARTING_STATUS;
import org.bimrocket.ihub.connector.Processor;
import org.bimrocket.ihub.exceptions.InvalidSetupException;

/**
 *
 * @author realor
 */
@Component
@ViewScoped
public class ConnectorListBean
{
  @Autowired
  ConnectorService connectorService;

  @Autowired
  ProcessorService processorService;

  @Autowired
  ConnectorMapperService connectorMapperService;

  @Autowired
  ConnectorBean connectorBean;

  @Autowired
  ProcessorBean processorBean;

  @Autowired
  PropertyBean propertyBean;

  TreeNode rootNode = new DefaultTreeNode("root", "Inventories", null);
  TreeNode selectedNode;
  String connectorName;
  String operation;
  Set<String> changed = new HashSet<>();
  boolean connectorRunning;

  public void search()
  {
    rootNode = findConnectors(connectorName);
    changed.clear();
  }

  public String getConnectorName()
  {
    return connectorName;
  }

  public void setConnectorName(String connectorName)
  {
    this.connectorName = connectorName;
  }

  public TreeNode getSelectedNode()
  {
    return selectedNode;
  }

  public void setSelectedNode(TreeNode selectedNode)
  {
    this.selectedNode = selectedNode;
  }

  public void setNodes(TreeNode rootNode)
  {
    this.rootNode = rootNode;
  }

  public TreeNode getNodes()
  {
    return rootNode;
  }

  public boolean isConnectorChanged()
  {
    if (selectedNode == null) return false;

    Object data = selectedNode.getData();
    if (data instanceof ConnectorSetup)
    {
      ConnectorSetup connSetup = (ConnectorSetup)data;
      return changed.contains(connSetup.getName());
    }
    return false;
  }

  public boolean isConnectorChanged(ConnectorSetup connSetup)
  {
    if (connSetup == null) return false;
    return changed.contains(connSetup.getName());
  }

  // connector operations

  public void addConnector()
  {
    ConnectorSetup connSetup = new ConnectorSetup();
    connectorBean.setConnectorSetup(connSetup);
    operation = "add";
  }

  public void editConnector()
  {
    ConnectorSetup connSetup = new ConnectorSetup();
    ConnectorSetup curConnSetup = (ConnectorSetup)selectedNode.getData();
    curConnSetup.copyTo(connSetup);
    connectorBean.setConnectorSetup(connSetup);
    operation = "edit";
  }

  public void putConnector(ConnectorSetup connSetup) throws Exception
  {
    if ("add".equals(operation))
    {
      Connector connector =
        connectorService.createConnector(connSetup.getName());
      connectorMapperService.setConnectorSetup(connector, connSetup);
      createTreeNode(connSetup, rootNode);
      changed.remove(connSetup.getName());
    }
    else // edit
    {
      ConnectorSetup curConnSetup = (ConnectorSetup)selectedNode.getData();
      connSetup.copyTo(curConnSetup);
      changed.add(connSetup.getName());
    }
  }

  public void applyConnectorChanges()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      Connector connector = connectorService.getConnector(connSetup.getName());

      connectorMapperService.setConnectorSetup(connector, connSetup);
      changed.remove(connSetup.getName());
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void undoConnectorChanges()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      Connector connector = connectorService.getConnector(connSetup.getName());

      connSetup = connectorMapperService.getConnectorSetup(connector);

      TreeNode connNode = createTreeNode(connSetup, null);
      connNode.setExpanded(true);

      int index = rootNode.getChildren().indexOf(selectedNode);
      rootNode.getChildren().set(index, connNode);

      changed.remove(connSetup.getName());
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void saveConnector()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
      Connector connector = connectorService.getConnector(connSetup.getName());
      connectorMapperService.setConnectorSetup(connector, connSetup);
      connector.save();
      changed.remove(connSetup.getName());
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public void deleteConnector()
  {
    try
    {
      ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();

      connectorService.destroyConnector(connSetup.getName(), true);

      rootNode.getChildren().remove(selectedNode);
      selectedNode = null;
      changed.remove(connSetup.getName());
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public boolean getConnectorRunning()
  {
    Object data = FacesUtils.getExpressionValue("#{data}");
    if (data instanceof ConnectorSetup)
    {
      String name = ((ConnectorSetup)data).getName();
      try
      {
        Connector connector = connectorService.getConnector(name);
        String status = connector.getStatus();
        return RUNNING_STATUS.equals(status) || STARTING_STATUS.equals(status);
      }
      catch (NotFoundException ex)
      {
      }
    }
    return false;
  }

  public void setConnectorRunning(boolean running)
  {
    // status change is performed in listener
    connectorRunning = running;
  }

  public void connectorStatusChanged(FacesEvent event)
  {
    ConnectorSetup connSetup =
      (ConnectorSetup)FacesUtils.getExpressionValue("#{data}");

    String name = connSetup.getName();
    try
    {
      Connector connector = connectorService.getConnector(name);
      if (connectorRunning)
      {
        connector.start();
      }
      else
      {
        connector.stop();
      }
    }
    catch (NotFoundException ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  public String getSelectedConnectorName()
  {
    if (selectedNode != null)
    {
      if (selectedNode.getData() instanceof ConnectorSetup)
      {
        ConnectorSetup connSetup = (ConnectorSetup)selectedNode.getData();
        return connSetup.getName();
      }
    }
    return null;
  }

  // processor operations

  public String getProcessorDescription(ProcessorSetup procSetup)
  {
    String description = procSetup.getDescription();
    if (description == null || description.length() == 0)
    {
      description = procSetup.getClassName();
    }
    else
    {
      String className = procSetup.getClassName();
      int index = className.lastIndexOf(".");
      if (index > 0)
      {
        className = className.substring(index + 1);
      }
      description += " (" + className + ")";
    }
    return description;
  }

  public boolean isProcessorEnabled(ProcessorSetup procSetup)
  {
    if (procSetup == null || procSetup.getEnabled() == null) return false;
    return procSetup.getEnabled();
  }

  public void addProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setEnabled(Boolean.TRUE);
    processorBean.setProcessorSetup(procSetup);
    operation = "add";
  }

  public void insertProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    procSetup.setEnabled(Boolean.TRUE);
    processorBean.setProcessorSetup(procSetup);
    operation = "insert";
  }

  public void editProcessor()
  {
    ProcessorSetup procSetup = new ProcessorSetup();
    ProcessorSetup curProcSetup = (ProcessorSetup)selectedNode.getData();
    curProcSetup.copyTo(procSetup);
    processorBean.setProcessorSetup(procSetup);
    operation = "edit";
  }

  public void putProcessor(ProcessorSetup procSetup) throws Exception
  {
    ConnectorSetup connSetup;
    if ("add".equals(operation))
    {
      TreeNode connNode = selectedNode;
      connSetup = (ConnectorSetup)connNode.getData();

      setDefaultProperties(procSetup);

      connSetup.getProcessors().add(procSetup);

      createTreeNode(procSetup, selectedNode);
      connNode.setExpanded(true);
    }
    else if ("insert".equals(operation))
    {
      TreeNode connNode = selectedNode.getParent();
      connSetup = (ConnectorSetup)connNode.getData();

      ProcessorSetup selProcSetup = (ProcessorSetup)selectedNode.getData();

      int index = connSetup.getProcessors().indexOf(selProcSetup);

      setDefaultProperties(procSetup);

      connSetup.getProcessors().add(index, procSetup);

      TreeNode procNode = createTreeNode(procSetup, null);
      connNode.getChildren().add(index, procNode);
    }
    else // edit
    {
      ProcessorSetup curProcSetup = (ProcessorSetup)selectedNode.getData();
      procSetup.copyTo(curProcSetup);

      TreeNode connNode = selectedNode.getParent();
      connSetup = (ConnectorSetup)connNode.getData();
    }
    changed.add(connSetup.getName());
  }

  public void deleteProcessor()
  {
    try
    {
      TreeNode connNode = selectedNode.getParent();
      ConnectorSetup connSetup = (ConnectorSetup)connNode.getData();

      ProcessorSetup procSetup = (ProcessorSetup)selectedNode.getData();
      connSetup.getProcessors().remove(procSetup);

      connNode.getChildren().remove(selectedNode);
      selectedNode = null;
      changed.add(connSetup.getName());
    }
    catch (Exception ex)
    {
      FacesUtils.addErrorMessage(ex);
    }
  }

  // property operations

  public String getPropertyValue(TreeNode propNode)
  {
    TreeNode procNode = propNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    ProcessorProperty property = (ProcessorProperty)propNode.getData();

    Object value = procSetup.getProperties().get(property.getName());
    if (value == null) return "";

    if (property.isSecret()) return "******";

    String textValue = String.valueOf(value);

    if (textValue.length() > 60)
    {
      textValue = textValue.substring(0, 50) + "...";
    }

    return textValue;
  }

  public void editProperty()
  {
    ProcessorProperty property = (ProcessorProperty)selectedNode.getData();
    TreeNode procNode = selectedNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    Object value = procSetup.getProperties().get(property.getName());

    propertyBean.setProperty(property);
    propertyBean.setValue(value);
  }

  public void putProperty(Object value)
  {
    TreeNode propNode = selectedNode;
    ProcessorProperty property = (ProcessorProperty)propNode.getData();
    TreeNode procNode = propNode.getParent();
    ProcessorSetup procSetup = (ProcessorSetup)procNode.getData();
    procSetup.getProperties().put(property.getName(), value);
    TreeNode connNode = procNode.getParent();
    ConnectorSetup connSetup = (ConnectorSetup)connNode.getData();
    changed.add(connSetup.getName());
  }

  // other methods

  public void onNodeExpand(NodeExpandEvent event)
  {
  }

  public void onNodeCollapse(NodeCollapseEvent event)
  {
  }

  private TreeNode findConnectors(String name)
  {
    rootNode = new DefaultTreeNode("root", "Inventories", null);

    ArrayList<Connector> connectors =
      connectorService.getConnectorsByName(name);

    connectors.forEach(connector ->
    {
      ConnectorSetup connSetup =
        connectorMapperService.getConnectorSetup(connector);

      createTreeNode(connSetup, rootNode);
    });
    return rootNode;
  }

  private TreeNode createTreeNode(ConnectorSetup connSetup, TreeNode rootNode)
  {
    TreeNode connNode = new DefaultTreeNode(connSetup);
    connNode.setType("connector");

    if (rootNode != null)
    {
      rootNode.getChildren().add(connNode);
    }

    List<ProcessorSetup> procSetups = connSetup.getProcessors();
    procSetups.forEach(procSetup ->
    {
      createTreeNode(procSetup, connNode);
    });
    return connNode;
  }

  private TreeNode createTreeNode(ProcessorSetup procSetup, TreeNode connNode)
  {
    TreeNode procNode = new DefaultTreeNode(procSetup);
    procNode.setType("processor");

    if (connNode != null)
    {
      connNode.getChildren().add(procNode);
    }

    try
    {
      ProcessorType processorType =
        processorService.getProcessorType(procSetup.getClassName());

      List<ProcessorProperty> properties = processorType.getProperties();
      properties.forEach(property ->
      {
        TreeNode propNode = new DefaultTreeNode("property",
          property, procNode);
      });
    }
    catch (InvalidSetupException ex)
    {
      // ignore
    }
    return procNode;
  }

  private void setDefaultProperties(ProcessorSetup procSetup)
  {
    try
    {
      String className = procSetup.getClassName();
      Processor processor = processorService.createProcessor(className);

      procSetup.setProperties(
        connectorMapperService.getProcessorProperties(processor));
    }
    catch (InvalidSetupException ex)
    {
      // ignore
    }
  }
}
