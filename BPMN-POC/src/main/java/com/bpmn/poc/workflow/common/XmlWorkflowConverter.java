/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.common;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;
import java.io.Reader;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlWorkflowConverter implements WorkflowConverter, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(XmlWorkflowConverter.class);

    final String fileExt;

    public XmlWorkflowConverter(final String ext) {
        this.fileExt = ext;
    }

    public XmlWorkflowConverter() {
        this(DEFAULT_EXT);
    }

    @Override
    public Workflow convert(Reader stream, String name) throws FlowExecutionException {
        Validation.requireNonNull(stream, () -> new FlowExecutionException("InputStream can not be null"));

        logger.debug("Converting workflow {} from xml to java object", name);

        return new Workflow("", "");
    }

    @Override
    public String getFileExt() {
        return this.fileExt;
    }

}
