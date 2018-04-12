/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.cache;

import java.io.Serializable;

/**
 * Defines cache's types which can be applied to ActionNode
 *
 */
public enum ActionBlockCache implements Serializable {

    /**
     * Creates just one instance of ActionBlock per WorkflowEngine
     */
    SINGLETON,
    /**
     * Creates one instance of ActionBlock for each uuid
     */
    NODE,
    /**
     * Creates new instance of ActionBlock for each call
     */
    NONE;

    private static final long serialVersionUID = 1L;

}
