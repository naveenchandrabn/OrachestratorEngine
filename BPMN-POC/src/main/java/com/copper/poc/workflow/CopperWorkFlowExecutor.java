/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.copper.poc.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.copperengine.core.WorkflowInstanceDescr;
import org.copperengine.core.persistent.PersistentScottyEngine;

/**
 *
 * @author jaja0617
 */
public class CopperWorkFlowExecutor {

    private static final String WF_CLASS = "com.copper.poc.workflow.dir.CopperWorkflow";

    private CopperEngineFactory factory;
    private final AtomicInteger counter = new AtomicInteger();
    private final String payload;

    public CopperWorkFlowExecutor(int payloadSize) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < payloadSize; i++) {
            sb.append(i % 10);
        }
        payload = sb.toString();
    }

    public synchronized CopperWorkFlowExecutor start() throws Exception {
        if (factory != null) {
            return this;
        }

        factory = new CopperEngineFactory();
        factory.getEngine().startup();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                factory.destroyEngine();
            }
        });
        return this;
    }

    public CopperWorkFlowExecutor startThread() {
        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    work();
                }
            }
        }.start();
        return this;
    }

    public CopperWorkFlowExecutor startSingleThread() {
        new Thread() {
            @Override
            public void run() {
                singleWork();
            }
        }.start();
        return this;
    }

    public void work() {
        try {
            final PersistentScottyEngine engine = factory.getEngine();
            List<String> cids = new ArrayList<>();
            for (int i = 0; i < 2000; i++) {
                final String cid = engine.createUUID();
                final LoadTestData data = new LoadTestData(cid, payload);
                final WorkflowInstanceDescr<LoadTestData> wfid = new WorkflowInstanceDescr<>(WF_CLASS, data, cid, 1, null);
                engine.run(wfid);
                cids.add(cid);
            }
            for (String cid : cids) {
                factory.getBackchannel().wait(cid, 5, TimeUnit.MINUTES);
                int value = counter.incrementAndGet();
                if (value % 10000 == 0) {
                    System.out.println(new Date() + " - " + value + " workflow instances processed so far.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void singleWork() {
        try {
            final PersistentScottyEngine engine = factory.getEngine();
            final String cid = engine.createUUID();
            final LoadTestData data = new LoadTestData(cid, payload);
            final WorkflowInstanceDescr<LoadTestData> wfid = new WorkflowInstanceDescr<>(WF_CLASS, data, cid, 1, null);
            engine.run(wfid);
            factory.getBackchannel().wait(cid, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        try {
            CopperWorkFlowExecutor exe = new CopperWorkFlowExecutor(4096);
            exe.start();
            exe.startThread();
            System.out.println("Started!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
