/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

import com.bpmn.poc.workflow.node.WorkflowNode.NodeInfo;

public interface ActionHandler  {

    public default void preExecute(NodeInfo nodeInfo, FlowContext context, ActionBlock actionBlock) {

    }

    public default void postExecute(NodeInfo nodeInfo, FlowContext context, ActionBlock actionBlock) {

    }

}
