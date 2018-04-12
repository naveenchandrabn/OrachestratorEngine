/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.common;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;
import java.io.Reader;

/**
 * Converts xml/json/... representation of workflow into java object
 *
 */
public interface WorkflowConverter {
	
	public final static String DEFAULT_EXT = ".n4j";
	
	Workflow convert(Reader reader, String name) throws FlowExecutionException;
	
	String getFileExt();

}
