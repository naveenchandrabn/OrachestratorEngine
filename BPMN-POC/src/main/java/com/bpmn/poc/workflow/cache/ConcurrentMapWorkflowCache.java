/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.cache;

import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.Workflow;
import com.bpmn.poc.workflow.loader.WorkflowLoader;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache implementation based on ConcurrentHashMap
 */
public class ConcurrentMapWorkflowCache implements WorkflowCache, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentMapWorkflowCache.class);

    private final ConcurrentMap<String, WorkflowProxy> cache;

    public ConcurrentMapWorkflowCache(final ConcurrentMap<String, WorkflowProxy> cache) {
        this.cache = cache;
    }

    public ConcurrentMapWorkflowCache() {
        this(new ConcurrentHashMap<String, WorkflowProxy>());
    }

    @Override
    public void clearAll() {
        cache.clear();
    }

    @Override
    public Workflow get(final WorkflowLoader loader, final String location) throws FlowExecutionException {
        // Note : Anonymous class cannot be serilized hence should not be used 
        /*WorkflowProxy entry = cache.computeIfAbsent(location, new Function<String, WorkflowProxy>() {

            @Override
            public WorkflowProxy apply(String location) {
                return new WorkflowProxy(location);
            }
        });
        return entry.get(loader);*/
        WorkflowProxy proxy = cache.get(location);
        if (proxy != null) {
            return proxy.get(loader);
        }
        return ((new WorkflowProxy(location)).get(loader));

    }

    @Override
    public void clear(String key) {
        cache.remove(key);
    }

    /*public class WorkflowProxy implements Serializable {

        private static final long serialVersionUID = 1L;
        private Object lock = new Object();
        private Workflow workflow;
        private String location;

        public WorkflowProxy(String location) {
            this.location = location;
        }

        public Workflow get(WorkflowLoader loader) throws FlowExecutionException {
            if (workflow != null) {
                logger.debug("Found in cache: {}", location);
                return workflow;
            }
            synchronized (lock) {
                if (workflow != null) {
                    logger.debug("Found in cache: {}", location);
                    return workflow;
                }
                logger.debug("Loading to cache: {}", location);
                workflow = loader.load(location);
            }
            return workflow;
        }

    }*/
}
