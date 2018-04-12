/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bpmn.poc.workflow.node;

import com.bpmn.poc.workflow.FlowContext;
import com.bpmn.poc.workflow.FlowExecutionException;
import com.bpmn.poc.workflow.WorkflowRequest;
import com.bpmn.poc.workflow.loader.SWFConstants;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.ConstructorUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for executable nodes
 *
 */
public class WorkflowNode implements Serializable{

 	private static final long serialVersionUID = 1L;
        private static final Logger logger = LoggerFactory.getLogger(WorkflowNode.class);

	final private Map<String, String> parameters = new HashMap<String, String>(4);

	final Map<String, Transition> exits = new HashMap<String, Transition>(3);

	private final NodeInfo nodeInfo;

	public WorkflowNode(String name, String uuid) {
		nodeInfo = new NodeInfo(uuid, name);
	}

	public Set<String> getParameterNames() {
		return parameters.keySet();
	}

	public void addParameter(String key, String value) {
		this.parameters.put(key, value);
	}

	public String getParameter(String key) {

		return this.parameters.get(key);
	}

	public String getName() {
		return nodeInfo.getName();
	}

	public String getUuid() {
		return nodeInfo.getUuid();
	}

	public NodeInfo getNodeInfo() {
		return nodeInfo;
	}

	public Transition getExitByName(String relationName) {
		return exits.get(relationName);
	}

	public void registerExit(Transition con) {
		con.setFromNode(this);
		exits.put(con.getName(), con);

	}

	public Collection<Transition> getExits() {
		return exits.values();
	}

	/**
	 * Validates if current node can be executed
	 * 
	 * @param ctx
	 *            current context
	 * @param processor
	 *            current processor
	 * @param ctx
	 *            current context
	 * @throws FlowExecutionException
	 *             in case of error
	 */
	public void validate(final WorkflowProcessor processor, final FlowContext ctx) throws FlowExecutionException {
		return;
	}

	/**
	 * Executes current node.
	 * 
	 * @param processor
	 *            workflow processor
	 * @param request
	 *            current request
	 * @return next transition
	 * @throws FlowExecutionException
	 *             in case of error
	 */
	protected Transition execute(final WorkflowProcessor processor, final WorkflowRequest request)
			throws FlowExecutionException {
		return null;
	}

	public void init() throws FlowExecutionException {

	}

	protected final void evaluateParameterValue(String source, String target, FlowContext ctx) {
		Object obj = null;

		// 1) if null
		if (SWFConstants.NULL_VALUE.equalsIgnoreCase(source)) {
			ctx.put(target, null);
			return;

			// 2) if create new class expression
		} else if (source.startsWith(SWFConstants.NEW_CLASS_SYMBOL_START)
				&& source.endsWith(SWFConstants.NEW_CLASS_SYMBOL_END)) {

			source = source.replace(SWFConstants.QUOTES_SYMBOL, "").replace("(", "").replace(")", "");

			obj = createNewInstance(source);

			ctx.put(target, obj);
			return;
		}

		String[] parts = source.split("\\+");

		// if concatenated string
		if (parts.length > 1) {
			StringBuilder stringValue = new StringBuilder();

			for (String src : parts) {
				stringValue.append(ctx.get(src));
			}
			obj = stringValue.toString();

		} else {
			obj = ctx.get(source);
		}

		ctx.put(target, obj);

	}

	private Object createNewInstance(String clazzName) {
		Class<?> beanClass = null;
		Object beanInstance = null;
		try {
			beanClass = getClass().getClassLoader().loadClass(clazzName);
			beanInstance = ConstructorUtils.invokeConstructor(beanClass, null);
		} catch (Exception e) {
			logger.error("Error during creating class" + clazzName, e);
		}

		return beanInstance;

	}

	/**
	 * Provides immutable information about node
	 *
	 */
	static public class NodeInfo {
		private final String uuid;
		private final String name;

		public NodeInfo(String uuid, String name) {
			super();
			this.uuid = uuid;
			this.name = name;
		}

		public String getUuid() {
			return uuid;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "NodeInfo [uuid=" + uuid + ", name=" + name + "]";
		}

	}

}
