/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow;

import com.bpmn.poc.workflow.common.Validation;
import java.io.Serializable;
import org.apache.commons.beanutils.ConstructorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides default implementation of CustomBlockInitStrategy and
 * initializes ActionBlock by calling default constructor
 *
 */
public class DefaultCustomBlockInitStrategy implements CustomBlockInitStrategy, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DefaultCustomBlockInitStrategy.class);

    @Override
    public ActionBlock loadCustomBlock(String className) throws FlowExecutionException {
        Class<? extends ActionBlock> clazz = getCustomBlockClass(className);
        try {
            if (null != clazz) {
                ActionBlock action = (ActionBlock) ConstructorUtils.invokeConstructor(clazz, null);
                if (action != null) {
                    return action;
                }
            }

        } catch (Exception e) {
            logger.error("Error during loading custom block " + className, e);
        }
        throw new FlowExecutionException("CustomBlock: " + className + " can not be initialized.");
    }

    public Class<? extends ActionBlock> getCustomBlockClass(String className) throws FlowExecutionException {
        Validation.requireNonNull(className, () -> new FlowExecutionException("CustomClassName can not be null"));
        try {
            Class<?> clazz = getClass().getClassLoader().loadClass(className);
            if (null != clazz) {
                if (ActionBlock.class.isAssignableFrom(clazz)) {
                    if (clazz != null) {
                        return (Class<? extends ActionBlock>) clazz;
                    }
                } else {
                    throw new FlowExecutionException(className + " does not implement org.neuro4j.workflow.ActionBlock");
                }
            }

        } catch (ClassNotFoundException e) {
            logger.error(className + " not found", e);
        }
        throw new FlowExecutionException("CustomBlock: " + className + " can not be initialized.");
    }

}
