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

import java.io.Serializable;

public class Transition implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private WorkflowNode fromNode;
    private WorkflowNode toNode;

    public Transition() {
    }

    public void setFromNode(WorkflowNode fromNode) {
        this.fromNode = fromNode;
    }

    public WorkflowNode getToNode() {
        return toNode;
    }

    public void setToNode(WorkflowNode toNode) {
        this.toNode = toNode;
    }

    public boolean isValid() {
        if (fromNode == null || toNode == null) {
            return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Transition [name=" + name + ", fromNode=" + fromNode + ", toNode=" + toNode + "]";
    }

}
