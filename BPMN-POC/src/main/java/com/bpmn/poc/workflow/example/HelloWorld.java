package com.bpmn.poc.workflow.example;

import com.bpmn.poc.workflow.ActionBlock;
import static com.bpmn.poc.workflow.ActionBlock.NEXT;
import com.bpmn.poc.workflow.FlowContext;
import com.bpmn.poc.workflow.FlowExecutionException;
import static com.bpmn.poc.workflow.cache.ActionBlockCache.SINGLETON;
import com.bpmn.poc.workflow.cache.CachedNode;
import com.bpmn.poc.workflow.common.ParameterDefinition;
import com.bpmn.poc.workflow.common.ParameterDefinitionList;
import static com.bpmn.poc.workflow.example.HelloWorld.IN_NAME;
import static com.bpmn.poc.workflow.example.HelloWorld.OUT_MESSAGE;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HelloWorld block receives name as input parameter and returns message as
 * output parameters.
 *
 */
@ParameterDefinitionList(input = {
    @ParameterDefinition(name = IN_NAME, isOptional = true, type = "java.lang.String")},
        output = {
            @ParameterDefinition(name = OUT_MESSAGE, isOptional = true, type = "java.lang.String")})

// will create  just one instance  of HelloWorld's class in workflow
@CachedNode(type = SINGLETON)
public class HelloWorld implements ActionBlock, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(HelloWorld.class);
    static final String IN_NAME = "name";

    static final String OUT_MESSAGE = "message";

    public int execute(FlowContext ctx) throws FlowExecutionException {

        String name = (String) ctx.get(IN_NAME);

        String message = "Hello World! ";

        if (name != null) {
            message += name;
        }

        logger.debug("Message: {}", message);

        ctx.put(OUT_MESSAGE, message);

        return NEXT;
    }

}
