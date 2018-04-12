/**
 * Copyright (c) 2013-2016, Neuro4j
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.bpmn.poc.workflow;

import com.bpmn.poc.workflow.node.Transition;
import com.bpmn.poc.workflow.node.WorkflowNode.NodeInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;

/**
 * Holds information about current request
 *
 */
public class WorkflowRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private HashMap<String, Iterator> loopIterator = null;
    private Stack<String> packages = new Stack<String>();
    private Transition nextTransition = null;
    private NodeInfo lastSuccessfulNode = null;
    private final List<CompletableFuture<?>> completableFutures = new ArrayList<>();
    private FlowContext logicContext;

    public WorkflowRequest() {
        this(new FlowContext());
    }

    public WorkflowRequest(Map<String, Object> map) {
        this(new FlowContext(map));
    }

    public WorkflowRequest(FlowContext logicContext) {
        super();
        loopIterator = new HashMap<String, Iterator>();
        this.logicContext = logicContext;
    }

    public void putLoopIterator(String aKey, Iterator aValue) {
        this.loopIterator.put(aKey, aValue);
    }

    public Iterator getLoopIterator(String aKey) {
        return this.loopIterator.get(aKey);
    }

    public void removeLoopIterator(String aKey) {
        this.loopIterator.remove(aKey);
    }

    public void setNextRelation(Transition transition) throws FlowExecutionException {
        if (transition == null) {
            throw new FlowExecutionException("Next transition can not be null");
        }
        this.nextTransition = transition;
    }

    public Transition getNextWorkflowNode() {
        return nextTransition;
    }

    public void pushPackage(String flowPackage) {
        packages.push(flowPackage);
    }

    public String popPackage() {
        return packages.pop();
    }

    public String getCurrentPackage() {
        return packages.peek();
    }

    public NodeInfo getLastSuccessfulNode() {
        return lastSuccessfulNode;
    }

    public void setLastSuccessfulNode(NodeInfo lastSuccessfulNode) {
        this.lastSuccessfulNode = lastSuccessfulNode;
    }

    public FlowContext getLogicContext() {
        return logicContext;
    }

    public void addParameter(String key, Object value) {
        logicContext.put(key, value);
    }

    public Locale getLocale() {
        return logicContext.getLocale();
    }

    public void setRequestLocale(Locale locale) {
        this.logicContext.setLocale(locale);
    }

    public List<CompletableFuture<?>> getCompletableFutures() {
        return completableFutures;
    }

    public void addCompletableFuture(List<CompletableFuture<ExecutionResult>> list) throws FlowExecutionException {
        if (!completableFutures.isEmpty()) {
            throw new FlowExecutionException("completableFutures list is not empty");
        }
        completableFutures.addAll(list);
    }

}
