/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.loader;

import com.bpmn.poc.workflow.ActionBlock;
import com.bpmn.poc.workflow.CustomBlockInitStrategy;
import com.bpmn.poc.workflow.DefaultCustomBlockInitStrategy;
import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.cache.ActionBlockCache;
import static com.bpmn.poc.workflow.cache.ActionBlockCache.NONE;
import com.bpmn.poc.workflow.cache.CachedNode;
import com.bpmn.poc.workflow.common.Validation;
import com.bpmn.poc.workflow.node.CustomNode;
import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and initializes custom (user's defined) blocks.
 *
 */
public class CustomBlockLoader implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CustomBlockLoader.class);

    private final ConcurrentHashMap<String, ActionBlock> cache;

    private final DefaultCustomBlockInitStrategy defaultInitStrategy = new DefaultCustomBlockInitStrategy();

    final private CustomBlockInitStrategy customBlockInitStrategy;

    public CustomBlockLoader(final CustomBlockInitStrategy customBlockInitStrategy, final ConcurrentHashMap<String, ActionBlock> cache) {
        super();
        this.customBlockInitStrategy = Optional.ofNullable(customBlockInitStrategy).orElse(defaultInitStrategy);
        this.cache = cache;
    }

    public CustomBlockLoader(final CustomBlockInitStrategy customBlockInitStrategy) {
        this(customBlockInitStrategy, new ConcurrentHashMap<>());

    }

    /**
     * Lookups customBlock by executable class. If block does not exist in cache
     * - creates instance and init. it.
     *
     * @param entity custom node
     * @return object implemented ActionBlock
     * @throws FlowExecutionException in case of error
     */
    public ActionBlock lookupBlock(CustomNode entity) throws FlowExecutionException {

        Validation.requireNonNull(entity.getExecutableClass(),
                () -> new FlowExecutionException("ExecutableClass not defined for CustomNode " + entity.getName()));

        long start = System.currentTimeMillis();

        ActionBlockCache cacheType = Optional.ofNullable(getCustomBlockClass(entity).getAnnotation(CachedNode.class))
                .map(n -> n.type())
                .orElseGet(() -> ActionBlockCache.NONE);
        ActionBlock block = null;

        switch (cacheType) {
            case SINGLETON:
                block = cache.get(entity.getExecutableClass());
                if (block == null) {
                    block = loadCustomNode(entity);
                    cache.put(entity.getExecutableClass(), block);
                }

                break;

            case NODE:
                block = cache.get(entity.getUuid());
                if (block == null) {
                    block = loadCustomNode(entity);
                    cache.put(entity.getUuid(), block);
                }
                break;

            case NONE:
                block = loadCustomNode(entity);
                break;

            default:
                throw new FlowExecutionException("Unknow cache type: " + cacheType);
        }

        logger.debug("CustomBlock {} loaded and initialized in {} ms", entity.getExecutableClass(),
                System.currentTimeMillis() - start);

        return block;
    }

    private ActionBlock loadCustomNode(CustomNode entity) throws FlowExecutionException {
        ActionBlock block = customBlockInitStrategy.loadCustomBlock(entity.getExecutableClass());
        block.init();
        return block;
    }

    public Class<? extends ActionBlock> getCustomBlockClass(CustomNode entity) throws FlowExecutionException {
        return defaultInitStrategy.getCustomBlockClass(entity.getExecutableClass());
    }

}
