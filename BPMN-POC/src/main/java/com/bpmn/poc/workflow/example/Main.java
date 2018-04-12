/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.example;

import com.bpmn.poc.workflow.common.*;
import com.bpmn.poc.workflow.ExecutionResult;
import com.bpmn.poc.workflow.common.BPMNEngine.ConfigBuilder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jaja0617
 */
public class Main implements Serializable {

    private static final long serialVersionUID = 1L;

    public static void main(String[] args) {

        // create engine with default configuration
        BPMNEngine engine = new BPMNEngine(new ConfigBuilder());

        // put input parameters
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", "Workflow");

        //execute flow
        //run flow - engine.execute("<your.package.flowName>-<StartNodeName>", parameters);
        ExecutionResult result = engine.execute("com.bpmn.poc.workflow.example.HelloFlow-Start", parameters);

        if (result.getException() == null) {
            String message = (String) result.getFlowContext().get("message");
            System.out.println("Message: " + message);

        } else {
            result.print();
        }

    }
}
