/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.loader;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;

/**
 * Base interface to load workflow.
 */
public interface WorkflowLoader {
	
	/**
	 * Loads workflow by name
	 * @param name of workflow
	 * @return Workflow
	 * @throws FlowExecutionException in case of workflow can not be loaded
	 */
	public Workflow load(final String name) throws FlowExecutionException;

}
