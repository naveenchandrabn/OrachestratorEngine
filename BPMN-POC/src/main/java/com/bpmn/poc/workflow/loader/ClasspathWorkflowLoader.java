/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.loader;

import com.bpmn.poc.workflow.common.WorkflowConverter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * Loads workflow from classpath.
 *
 */
public class ClasspathWorkflowLoader extends URLWorkflowLoader {

    private static final long serialVersionUID = 1L;
    public ClasspathWorkflowLoader(final WorkflowConverter converter) {
        super(converter);
    }

    @Override
    protected URL getResource(String name) throws IOException {
        String location = normalize(name);
        return getClass().getClassLoader().getResource(location + converter.getFileExt());
    }

}
