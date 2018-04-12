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
package com.bpmn.poc.workflow.node;

import com.bpmn.poc.workflow.ActionBlock;
import com.bpmn.poc.workflow.FlowContext;
import com.bpmn.poc.workflow.FlowExecutionException;
import java.io.Serializable;



/**
 * 
 * CustomBlock provides base implementation for user's blocks.
 * Users should extend this class during developing own blocks.
 * 
 */
public abstract class CustomBlock implements ActionBlock, Serializable {

	/**
     * Default constructor.
     */
    public CustomBlock() {
        super();
    }
    
    @Override
	public abstract int execute(FlowContext context) throws FlowExecutionException;

	@Override
	public void init() throws FlowExecutionException {
		
	}


}
