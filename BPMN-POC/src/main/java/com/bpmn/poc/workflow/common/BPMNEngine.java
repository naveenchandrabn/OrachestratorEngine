/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.common;

import com.bpmn.poc.workflow.CustomBlockInitStrategy;
import com.bpmn.poc.workflow.DefaultCustomBlockInitStrategy;
import com.bpmn.poc.workflow.ExecutionResult;
import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.FlowParameter;
import com.bpmn.poc.workflow.WorkflowRequest;
import com.bpmn.poc.workflow.cache.ConcurrentMapWorkflowCache;
import com.bpmn.poc.workflow.cache.WorkflowCache;
import com.bpmn.poc.workflow.loader.ClasspathWorkflowLoader;
import com.bpmn.poc.workflow.loader.WorkflowLoader;
import com.bpmn.poc.workflow.node.WorkflowProcessor;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * WorkflowEngine allows to execute workflow with some input parameters
 * </p>
 * <h2>
 * Getting Started:</h2>
 *
 * <pre>
 * WorkflowEngine engine = new WorkflowEngine();
 * ExecutionResult result = engine.execute("org.domain.workflow.SomeFlow-StartNode");
 * </pre>
 */
public class BPMNEngine implements Serializable {

    private static final long serialVersionUID = 1L;
    private final WorkflowProcessor workflowProcessor;

    public BPMNEngine(ConfigBuilder builder) {
        this.workflowProcessor = new WorkflowProcessor(builder);
    }

    /**
     * Default constructor with ConcurrentMapWorkflowCache and ClasspathLoader
     */
    public BPMNEngine() {
        this(new ConfigBuilder().withCustomBlockInitStrategy(new DefaultCustomBlockInitStrategy())
                .withWorkflowCache(new ConcurrentMapWorkflowCache())
                .withLoader(new ClasspathWorkflowLoader(new XmlWorkflowConverter())));
    }

    /**
     * Executes flow with default parameters
     *
     * @param flow name
     * @return execution result
     */
    public ExecutionResult execute(String flow) {
        return execute(flow, new HashMap<String, Object>());
    }

    /**
     * Executes flow with default parameters
     *
     * @param flow name
     * @param params input parameters
     * @return execution result
     */
    public ExecutionResult execute(String flow, Map<String, Object> params) {
        WorkflowRequest request = new WorkflowRequest();

        if (null != params && !params.isEmpty()) {
            for (String key : params.keySet()) {
                request.addParameter(key, params.get(key));
            }
        }
        return workflowProcessor.execute(flow, request);
    }

    /**
     * Executes flow with given request
     *
     * @param flow name
     * @param request object
     * @return execution result
     */
    public ExecutionResult execute(String flow, WorkflowRequest request) {
        return workflowProcessor.execute(flow, request);
    }

    /**
     * Config class for engine
     */
    public static class ConfigBuilder implements Serializable {

        private static final long serialVersionUID = 12L;
        private WorkflowLoader loader;

        private CustomBlockInitStrategy customInitStrategy;

        private WorkflowCache workflowCache;

        transient private ActionHandlersRegistry registry;

        private final WorkflowConverter converter;

        private final Map<String, FlowParameter> aliases = new HashMap<>();

        public ConfigBuilder() {
            converter = new XmlWorkflowConverter();
        }

        public ConfigBuilder withLoader(WorkflowLoader loader) {
            this.loader = loader;
            return this;
        }

        public ConfigBuilder withCustomBlockInitStrategy(CustomBlockInitStrategy customInitStrategy) {
            this.customInitStrategy = customInitStrategy;
            return this;
        }

        public ConfigBuilder withWorkflowCache(WorkflowCache cache) {
            this.workflowCache = cache;
            return this;
        }

        public ConfigBuilder withActionRegistry(ActionHandlersRegistry registry) {
            this.registry = registry;
            return this;
        }

        public ConfigBuilder withAliases(final Map<String, String> aliases) throws FlowExecutionException {
            if (aliases == null || aliases.isEmpty()) {
                return this;
            }
            for (String key : aliases.keySet()) {
                FlowParameter param = FlowParameter.parse(aliases.get(key));
                this.aliases.put(key, param);
            }

            return this;
        }

        public WorkflowLoader getLoader() {
            loader = Optional.ofNullable(loader).orElse(new ClasspathWorkflowLoader(converter));
            return loader;
        }

        public CustomBlockInitStrategy getCustomInitStrategy() {
            customInitStrategy = Optional.ofNullable(customInitStrategy).orElse(new DefaultCustomBlockInitStrategy());
            return customInitStrategy;
        }

        public WorkflowCache getWorkflowCache() {
            workflowCache = Optional.ofNullable(workflowCache).orElse(new ConcurrentMapWorkflowCache());
            return workflowCache;
        }

        public ActionHandlersRegistry getActionRegistry() {
            this.registry = Optional.ofNullable(this.registry).orElse(new ActionHandlersRegistry(Collections.emptyMap()));
            return this.registry;
        }

        public Map<String, FlowParameter> getAliases() {
            return this.aliases;
        }

    }

}
