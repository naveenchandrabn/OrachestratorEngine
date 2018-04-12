    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

import java.io.Serializable;

/**
 * Base interface for executable blocks.
 *
 */
public interface ActionBlock {

    public static final int NEXT = 1;
    public static final int ERROR = 2;

    /**
     * @param context input context
     * @return 1 (NEXT) or 2 (ERROR) exit
     * @throws FlowExecutionException in case of error during execution
     */
    public int execute(FlowContext context) throws FlowExecutionException;

    /**
     * Processor run this method once during initialization.
     *
     * @throws FlowExecutionException if there is an error
     */
    public default void init() throws FlowExecutionException {

    }

}
