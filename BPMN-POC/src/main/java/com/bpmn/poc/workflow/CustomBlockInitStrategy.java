/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

/**
 * Defines method to load CustomBlock
 *
 */
public interface CustomBlockInitStrategy {

    public ActionBlock loadCustomBlock(String className) throws FlowExecutionException;

}
