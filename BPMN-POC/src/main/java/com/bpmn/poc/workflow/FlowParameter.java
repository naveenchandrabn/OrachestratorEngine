/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

import com.bpmn.poc.workflow.common.Validation;
import java.io.Serializable;

/**
 * Represents flow request. Contains full flow's name, package and request's
 * start node
 *
 */
public class FlowParameter implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String flowName;
    private final String flowPackage;
    private final String startNode;

    private FlowParameter(String name, String flowPackage, String startNode) {
        this.flowName = name;
        this.flowPackage = flowPackage;
        this.startNode = startNode;
    }

    /**
     * Parses request(ex. org.mydomain.MyFlow-MyStartNode)
     *
     * @param request parameter
     * @return FlowParameter object
     * @throws FlowExecutionException in case of wrong format
     */
    public static FlowParameter parse(String request) throws FlowExecutionException {
        Validation.requireNonNull(request, () -> new FlowExecutionException("Request flow can not be null"));

        String[] array = request.split("-");

        if (array.length < 1) {
            throw new FlowExecutionException("Incorrect flow name. Must be package.name.FlowName-StartNode");
        }

        String flowPackage = "default";

        String flow = array[0];

        String startNode = null;
        if (array.length == 2) {
            startNode = array[1];
        }
        int index = flow.lastIndexOf(".");

        if (index > 0) {
            flowPackage = flow.substring(0, index);
        }

        return new FlowParameter(flow, flowPackage, startNode);
    }

    public String getFlowName() {
        return flowName;
    }

    public String getFlowPackage() {
        return flowPackage;
    }

    public String getStartNode() {
        return startNode;
    }

    @Override
    public String toString() {
        return "FlowParameter [flowName=" + flowName + ", flowPackage=" + flowPackage + ", startNode=" + startNode
                + "]";
    }

}
