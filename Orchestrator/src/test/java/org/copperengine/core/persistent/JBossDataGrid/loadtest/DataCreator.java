/*
 * Copyright 2002-2015 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent.JBossDataGrid.loadtest;

import java.util.Collections;
import java.util.List;

import org.copperengine.core.Workflow;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.ScottyDBStorageInterface;
import org.copperengine.core.persistent.hybrid.HybridDBStorage;

public class DataCreator {

    public static void main(final String[] args) {
        final LoadTestCassandraEngineFactory factory = new LoadTestCassandraEngineFactory() {
            @Override
            protected ScottyDBStorageInterface createDBStorage() {
                return new HybridDBStorage(serializer.get(), workflowRepository.get(), storage.get(), timeoutManager.get(), executorService.get()) {
                    @Override
                    public List<Workflow<?>> dequeue(String ppoolId, int max) throws Exception {
                        return Collections.emptyList();
                    }
                };
            }
        };
        try {
            factory.getEngine().startup();
            createData(factory.getEngine());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            factory.destroyEngine();
        }
    }

    private static void createData(PersistentScottyEngine engine) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
            sb.append(i % 10);
        }
        final String payload = sb.toString();

        for (int i = 0; i < 500000; i++) {
            final String id = engine.createUUID();
            final LoadTestData data = new LoadTestData();
            data.id = id;
            data.someData = payload;
            final WorkflowInstanceDescr<LoadTestData> wfInstanceDescr = new WorkflowInstanceDescr<LoadTestData>("org.copperengine.core.persistent.JBossDataGrid.loadtest.workflows", data, id, 1, null);
            engine.run(wfInstanceDescr);
        }
    }
}
