/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.node;

import com.bpmn.poc.workflow.ActionBlock;
import com.bpmn.poc.workflow.ActionHandler;
import com.bpmn.poc.workflow.FlowContext;
import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.WorkflowRequest;
import com.bpmn.poc.workflow.common.ParameterDefinition;
import com.bpmn.poc.workflow.common.ParameterDefinitionList;
import static com.bpmn.poc.workflow.loader.SWFConstants.NEXT_RELATION_NAME;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds information about user's defined block. CustomNode has 2 exists - NEXT
 * and ERROR
 *
 */
public class CustomNode extends WorkflowNode {

    private static final Logger logger = LoggerFactory.getLogger(CustomNode.class);

    private static final String NEXT_EXIT_RELATION = NEXT_RELATION_NAME;
    private static final String ERROR_EXIT_RELATION = "ERROR";

    private final String executableClass;

    private Transition mainExit = null;
    private Transition errorExit = null;

    private final Map<String, String> outParameters;

    public CustomNode(String executableClass, String name, String uuid) {
        super(name, uuid);
        this.executableClass = executableClass;
        outParameters = new HashMap<String, String>(3);
    }

    public void addOutParameter(final String key, final String value) {
        outParameters.put(key, value);
    }

    public String getOutParameter(final String name) {
        return outParameters.get(name);
    }

    public final void init() throws FlowExecutionException {
        mainExit = getExitByName(NEXT_EXIT_RELATION);
        errorExit = getExitByName(ERROR_EXIT_RELATION);
    }

    @Override
    public final Transition execute(final WorkflowProcessor processor, final WorkflowRequest request) throws FlowExecutionException {
        FlowContext context = request.getLogicContext();

        ActionBlock actionBlock = processor.loadCustomBlock(this);

        //ActionHandler handler = processor.getActionHandler(actionBlock);

        //handler.preExecute(this.getNodeInfo(), context, actionBlock);

        int result = actionBlock.execute(context);

        //handler.postExecute(this.getNodeInfo(), context, actionBlock);

        if (result != CustomBlock.ERROR) {
            doOutputMapping(actionBlock, context);
            request.setNextRelation(mainExit);
        } else {
            if (errorExit == null) {
                throw new FlowExecutionException("CustomBlock " + getName() + ": Error connector not defined.");
            }
            request.setNextRelation(errorExit);
        }
        return request.getNextWorkflowNode();

    }

    @Override
    public final void validate(final WorkflowProcessor processor, final FlowContext ctx) throws FlowExecutionException {

        Class<? extends ActionBlock> customClass = processor.getCustomBlockClass(this);

        ParameterDefinitionList parameterDefinitionList = customClass.getAnnotation(ParameterDefinitionList.class);
        if (parameterDefinitionList == null) {
            return;
        }
        ParameterDefinition[] parameters = parameterDefinitionList.input();
        for (ParameterDefinition parameter : parameters) {
            String name = parameter.name();

            logger.debug("Processing input parameter: name - {} , type - {}", name, parameter.type());

            doInputMapping(ctx, name);

            Object obj = ctx.get(name);
            if (!parameter.isOptional()) {
                if (obj == null) {
                    throw new FlowExecutionException("Parameter " + name + " is mandatory for " + executableClass);
                }
            }
            checkPatameterType(parameter, obj);

        }

        if (mainExit == null) {
            throw new FlowExecutionException("CustomBlock " + getName() + ": Main connector not defined.");
        }
    }

    /**
     * Updates names of output parameters.
     *
     * @param cBlock current action object
     * @param ctx flow context
     * @throws FlowExecutionException
     */
    private void doOutputMapping(ActionBlock cBlock, FlowContext ctx) throws FlowExecutionException {
        ParameterDefinitionList parameterDefinitionList = cBlock.getClass().getAnnotation(ParameterDefinitionList.class);
        if (parameterDefinitionList == null) {
            return;
        }
        ParameterDefinition[] parameters = parameterDefinitionList.output();
        for (ParameterDefinition parameter : parameters) {
            String name = parameter.name();
            String key = doOutMapping(ctx, name);
            Object obj = ctx.get(key);
            if (!parameter.isOptional()) {
                if (obj == null) {
                    throw new FlowExecutionException("Parameter " + name + " is mandatory for " + getClass().getName());
                }

            }
            checkPatameterType(parameter, obj);

        }

    }

    /**
     * Updates names of input parameters.
     *
     * @param ctx flow context
     * @param originalName name of this parameter will be updated to new.
     */
    private void doInputMapping(FlowContext ctx, String originalName) {

        String mappedValue = getParameter(originalName);
        if (mappedValue != null && !mappedValue.equalsIgnoreCase(originalName)) {
            logger.debug("Mapping parameter: {} to  {}", mappedValue, originalName);

            evaluateParameterValue(mappedValue, originalName, ctx);
        }

    }

    /**
     * Method processes output parameters.
     *
     * @param ctx flow context
     * @param originalName name of output parameter
     * @return name old name of output parameter
     */
    private String doOutMapping(FlowContext ctx, String originalName) {

        String mappedValue = getOutParameter(originalName);
        if (mappedValue != null && !mappedValue.equalsIgnoreCase(originalName)) {
            Object obj = ctx.remove(originalName);
            ctx.put(mappedValue, obj);
            return mappedValue;
        }

        return originalName;

    }

    /**
     * Checks if type in parameterDefinition is the same with object's type.
     *
     * @param parameterDefinition annotation of ActionBlock
     * @param obj input parameter
     * @throws FlowExecutionException in case of error
     */
    private void checkPatameterType(ParameterDefinition parameterDefinition, Object obj) throws FlowExecutionException {
        if (obj == null) {
            return;
        }
        String className = parameterDefinition.type();
        if (className == null) {
            if (!parameterDefinition.isOptional()) {
                throw new FlowExecutionException("Type should be not empty for mandatory parameter" + parameterDefinition.name() + "(" + getName() + ")");
            }
            return;

        }
        if (className.equals(obj.getClass().getCanonicalName())) {
            return;
        }

        try {
            Class<?> cl = getClass().getClassLoader().loadClass(className);

            if (!cl.isAssignableFrom(obj.getClass())) {
                StringBuffer message = new StringBuffer("Wrong parameter type for ").append(parameterDefinition.name()).append("( ").append(getName()).append(" ). Expected type: ").append(className).append(" actual type: ").append(obj.getClass().getCanonicalName());
                throw new FlowExecutionException(message.toString());
            }
        } catch (ClassNotFoundException e) {

            if (className.contains(" ")) {
                logger.error("Class's name {} contains whitespace - please check your parameter with name: {}.", className, parameterDefinition.name());
            }

            throw new FlowExecutionException(e);
        }

    }

    /**
     * Returns name of executable class
     *
     * @return class name will be executed
     */
    public String getExecutableClass() {
        return executableClass;
    }

}
