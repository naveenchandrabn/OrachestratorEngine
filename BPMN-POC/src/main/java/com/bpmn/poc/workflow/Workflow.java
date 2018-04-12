/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

import com.bpmn.poc.workflow.node.StartNode;
import com.bpmn.poc.workflow.node.WorkflowNode;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

/**
 * Representation of single workflow unit. Holds information about all nodes in
 * current workflow
 */
public class Workflow implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Holds information about all Start nodes
     */
    private final HashMap<String, StartNode> startNodes = new HashMap<String, StartNode>();

    /**
     * Keeps all nodes
     */
    private final HashMap<String, WorkflowNode> nodes = new HashMap<String, WorkflowNode>();

    /**
     * FlowVisibility: can be Public or Private
     */
    private FlowVisibility visibility = FlowVisibility.getDefault();

    /**
     * Flow's paclage (ex. org.mydomain)
     */
    private final String flowPackage;

    /**
     * Flow name (ex. org.mydomain.MyFlow)
     */
    private final String flowName;

    public Workflow(String flowName, String flowPackage) {
        super();
        this.flowName = flowName;
        this.flowPackage = flowPackage;
    }

    /**
     * Returns package.
     *
     * @return package name
     */
    public String getPackage() {
        return flowPackage;
    }

    public StartNode getStartNode(String name) {
        return startNodes.get(name);

    }

    public boolean isPublic() {
        return visibility == FlowVisibility.Public;
    }

    public WorkflowNode getById(String uuid) {
        return nodes.get(uuid);
    }

    public void registerNode(WorkflowNode entity) {
        nodes.put(entity.getUuid(), entity);
    }

    public void registerStartNode(StartNode entity) {
        startNodes.put(entity.getName(), entity);
    }

    public void setVisibility(final FlowVisibility visibility) {
        this.visibility = Optional.ofNullable(visibility).orElse(FlowVisibility.getDefault());
    }

    /**
     * Returns flow name
     *
     * @return flow name
     */
    public String getFlowName() {
        return flowName;
    }

}
