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
package com.bpmn.poc.workflow.common;

import java.io.Serializable;
import java.lang.annotation.Annotation;

public class ParameterDefinitionImpl implements ParameterDefinition, Serializable {

    private static final long serialVersionUID = 1L;
    private final String name;
    private final String type;
    private final Boolean optional;

    public ParameterDefinitionImpl(String name, String type, Boolean optional) {
        this.name = name;
        this.type = type;
        this.optional = optional;
    }

    public Class<? extends Annotation> annotationType() {
        return ParameterDefinition.class;
    }

    public String name() {
        return name;
    }

    public String type() {
        return type;
    }

    public boolean isOptional() {
        return optional;
    }

}
