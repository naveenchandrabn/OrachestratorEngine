/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.common;

import com.bpmn.poc.workflow.ActionBlock;
import com.bpmn.poc.workflow.ActionHandler;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Contains all handlers for specific classes.
 *
 * Ex. Developer can define handler for custom block SystemOutBlock and this
 * handler will be called every time before method execute and after.
 *
 */
public class ActionHandlersRegistry implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Default handler with no implementation
     */
    transient final private ActionHandler defaultInstance = new ActionHandler() {
    };

    /**
     * Cache of all handlers
     */
    private final ConcurrentMap<Class<? extends ActionBlock>, ActionHandler> cache = new ConcurrentHashMap<Class<? extends ActionBlock>, ActionHandler>();

    public ActionHandlersRegistry(final Map<Class<? extends ActionBlock>, ActionHandler> map) {
        this.cache.putAll(map);
    }

    /**
     * Returns handler for requested class if it's exist or default handler
     * overwise.
     *
     * @param clazz class ? extends ActionBlock
     * @return handler for requested class
     */
    public ActionHandler get(final Class<? extends ActionBlock> clazz) {
        return Optional.ofNullable(cache.get(clazz)).orElse(defaultInstance);
    }
}
