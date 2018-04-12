/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.cache;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;
import com.bpmn.poc.workflow.loader.WorkflowLoader;

/**
 * Base interface for cache
 *
 */
public interface WorkflowCache {

	public void clearAll();

	public void clear(String key);

	public Workflow get(WorkflowLoader loader, String flow) throws FlowExecutionException;
}

