/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.node;

import com.bpmn.poc.workflow.ActionBlock;
import com.bpmn.poc.workflow.ActionHandler;
import com.bpmn.poc.workflow.DefaultCustomBlockInitStrategy;
import com.bpmn.poc.workflow.ExecutionResult;
import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.FlowParameter;
import com.bpmn.poc.workflow.Workflow;
import com.bpmn.poc.workflow.WorkflowRequest;
import com.bpmn.poc.workflow.cache.ConcurrentMapWorkflowCache;
import com.bpmn.poc.workflow.cache.WorkflowCache;
import com.bpmn.poc.workflow.common.ActionHandlersRegistry;
import com.bpmn.poc.workflow.common.BPMNEngine.ConfigBuilder;
import com.bpmn.poc.workflow.loader.CustomBlockLoader;
import com.bpmn.poc.workflow.common.Validation;
import com.bpmn.poc.workflow.common.XmlWorkflowConverter;
import com.bpmn.poc.workflow.loader.ClasspathWorkflowLoader;
import com.bpmn.poc.workflow.loader.WorkflowLoader;
import com.bpmn.poc.workflow.node.WorkflowNode.NodeInfo;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaja0617
 */
public class WorkflowProcessor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(WorkflowProcessor.class);

    transient private final ActionHandlersRegistry registry;

    private final WorkflowLoader loader;

    private final CustomBlockLoader customBlockLoader;

    private final WorkflowCache cache;

    private final Map<String, FlowParameter> aliases = new HashMap<>();

    /**
     * Constructor
     *
     * @param builder object with configuration
     */
    public WorkflowProcessor(ConfigBuilder builder) {
        this.loader = builder.getLoader();
        this.customBlockLoader = new CustomBlockLoader(builder.getCustomInitStrategy());
        this.cache = builder.getWorkflowCache();
        this.registry = builder.getActionRegistry();
        this.aliases.putAll(builder.getAliases());
    }


    /**
     * Executes flow by name with request's parameters
     *
     * @param flow name
     * @param request with parameters
     * @return execution result
     */
    public ExecutionResult execute(final String flow, final WorkflowRequest request) {

        ExecutionResult result = null;

        try {

            FlowParameter flowParameter = resolveFlow(flow);

            logger.debug("Loading flow: {}", flowParameter);

            long startLoading = System.currentTimeMillis();

            Workflow workflow = loadWorkflow(flowParameter.getFlowName());

            if (null == workflow) {
                throw new FlowExecutionException("Flow '" + flowParameter.getFlowName() + "' can't be loaded");
            }

            logger.debug("Loaded flow: {} in {} ms", flowParameter.getFlowName(), System.currentTimeMillis() - startLoading);

            result = execute(workflow, flowParameter.getStartNode(), request);

        } catch (FlowExecutionException ex) {
            logger.error(ex.getMessage(), ex);
            result = new ExecutionResult(request.getLogicContext());
            result.setExecutionExeption(ex);
        }

        return result;
    }

    public FlowParameter resolveFlow(String name) throws FlowExecutionException {
        FlowParameter param = aliases.get(name);
        if (param != null) {
            logger.debug("Alias  {} resolved to {}", name, param.toString());
        } else {
            param = FlowParameter.parse(name);
        }
        return param;
    }

    /**
     * Loads workflow from cache
     *
     * @param flowName name
     * @return flow object
     * @throws FlowExecutionException in case of error
     */
    public Workflow loadWorkflow(String flowName) throws FlowExecutionException {
        return cache.get(loader, flowName);
    }

    /**
     * Executes workflow object with given parameters.
     *
     * @param workflow
     * @param startNodeName
     * @param request
     * @return ExecutionResult
     */
    public ExecutionResult execute(final Workflow workflow, final String startNodeName, WorkflowRequest request) {

        long start = System.currentTimeMillis();

        request = Optional.ofNullable(request).orElse(new WorkflowRequest());

        ExecutionResult result = new ExecutionResult(request.getLogicContext());

        try {

            Validation.requireNonNull(workflow, () -> new FlowExecutionException("Flow can't be null"));

            Validation.requireNonNull(startNodeName, () -> new FlowExecutionException("StartNodeName can't be null"));

            StartNode startNode = workflow.getStartNode(startNodeName);

            if (null == startNode) {
                throw new FlowExecutionException(
                        "StartNode '" + startNodeName + "' not found in flow " + workflow.getFlowName());
            }

            /*if (!workflow.isPublic()) {
                throw new FlowExecutionException("Flow '" + workflow.getFlowName() + "' is not public");
            }

            if (!startNode.isPublic()) {
                throw new FlowExecutionException("Node '" + startNode.getName() + "' is not public");
            }*/
            request.pushPackage(workflow.getPackage());

            executeWorkflow(startNode, request);

            request.popPackage();
        } catch (FlowExecutionException ex) {
            logger.error(ex.getMessage(), ex);
            result.setExecutionExeption(ex);
        }

        NodeInfo lastNode = request.getLastSuccessfulNode();

        if (lastNode != null) {
            result.setLastSuccessfulNodeName(lastNode.getName());
        }

        logger.debug("Flow execution time: {} ms.", System.currentTimeMillis() - start);
        return result;
    }

    /**
     * Executes node with request's parameters
     *
     * @param firstNode node to be executed
     * @param request with parameters
     * @throws FlowExecutionException in case of error
     */
    final void executeWorkflow(WorkflowNode firstNode, WorkflowRequest request) throws FlowExecutionException {

        if (null == request) {
            throw new RuntimeException("WorkflowRequest must not be null");
        }
        WorkflowNode step = firstNode;
        while (null != step) {
            WorkflowNode lastNode = step;
            step = executeNode(step, request);
            request.setLastSuccessfulNode(lastNode.getNodeInfo());
            if (step != null) {
                logger.debug("Next step: {} ({})", step.getName(), step.getUuid());
            }
        }
    }

    /**
     * Executes next node with given parameters
     *
     * @param request with parameters
     * @return next workflow node
     * @throws FlowExecutionException in case of error
     */
    private final WorkflowNode executeNode(WorkflowNode node, WorkflowRequest request) throws FlowExecutionException {

        long startTime = System.currentTimeMillis();
        logger.debug("      Running: node {} ({})", node.getName(), node.getClass().getCanonicalName());
        node.validate(this, request.getLogicContext());
        //DebugService.getInstance().onNodeCall(node, request);
        Transition transition = node.execute(this, request);
        logger.debug("      Finished: node {} in ({} ms.)", node.getName(), (System.currentTimeMillis() - startTime));
        if (transition != null) {
            return transition.getToNode();
        }
        return null;
    }

    /**
     * Returns object implemented ActionBlock
     *
     * @param node CustomNode with executable class
     * @return object implemented ActionBlock
     * @throws FlowExecutionException
     */
    ActionBlock loadCustomBlock(CustomNode node) throws FlowExecutionException {
        return customBlockLoader.lookupBlock(node);
    }

    public ActionHandler getActionHandler(ActionBlock obj) {
        return registry.get(obj.getClass());
    }

    /**
     * Returns class object implemented ActionBlock
     *
     * @param node CustomNode with executable class
     * @return class object implemented ActionBlock
     * @throws FlowExecutionException
     */
    Class<? extends ActionBlock> getCustomBlockClass(CustomNode node) throws FlowExecutionException {
        return customBlockLoader.getCustomBlockClass(node);
    }
}
