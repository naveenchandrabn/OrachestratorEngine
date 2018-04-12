/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.cache;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;
import com.bpmn.poc.workflow.loader.WorkflowLoader;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowProxy implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(WorkflowProxy.class);

    private Object lock = new Object();
    private Workflow workflow;
    private String location;

    public WorkflowProxy(String location) {
        this.location = location;
    }

    public Workflow get(WorkflowLoader loader) throws FlowExecutionException {
        if (workflow != null) {
            logger.debug("Found in cache: {}", location);
            return workflow;
        }
        synchronized (lock) {
            if (workflow != null) {
                logger.debug("Found in cache: {}", location);
                return workflow;
            }
            logger.debug("Loading to cache: {}", location);
            workflow = loader.load(location);
        }
        return workflow;
    }

}
