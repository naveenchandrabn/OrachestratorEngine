/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.copper.poc.workflow.dir;

import com.bpmn.poc.workflow.ExecutionResult;
import com.bpmn.poc.workflow.common.BPMNEngine;
import com.copper.poc.workflow.DummyResponseSender;
import com.copper.poc.workflow.LoadTestData;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.AutoWire;
import org.copperengine.core.Interrupt;
import org.copperengine.core.Response;
import org.copperengine.core.WaitMode;
import org.copperengine.core.persistent.PersistentWorkflow;

import org.copperengine.core.util.Backchannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopperWorkflow extends PersistentWorkflow<LoadTestData> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CopperWorkflow.class);
    private static final int DEFAULT_TIMEOUT = 5000;

    private transient DummyResponseSender dummyResponseSender;
    private transient Backchannel backchannel;

    @AutoWire(beanId = "backchannel")
    public void setBackchannel(Backchannel backchannel) {
        this.backchannel = backchannel;
    }

    @AutoWire(beanId = "dummyResponseSender")
    public void setDummyResponseSender(DummyResponseSender dummyResponseSender) {
        this.dummyResponseSender = dummyResponseSender;
    }

    @Override
    public void main() throws Interrupt {
        try {

            logger.info("started");

            logger.info("Testing delayed response...");
            //delayedResponse();

            // create engine with default configuration
            BPMNEngine engine = new BPMNEngine(new BPMNEngine.ConfigBuilder());

            // put input parameters
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("name", "Workflow");

            //execute flow
            //run flow - engine.execute("<your.package.flowName>-<StartNodeName>", parameters);
            ExecutionResult result = engine.execute("com.bpmn.poc.workflow.example.HelloFlow-Start", parameters);

            if (result.getException() == null) {
                String message = (String) result.getFlowContext().get("message");
                //System.out.println("Message: " + message);

            } else {
                result.print();
            }
            delayedResponse();

            logger.info("Testing early response...");
            //earlyResponse();

            logger.info("Testing timeout response...");
            //timeoutResponse();

            logger.info("Testing delayed multi response...");
            //delayedMultiResponse();

            backchannel.notify(getData().id, "OK");
            logger.info("finished");
        } catch (Exception e) {
            logger.error("workflow failed", e);
            backchannel.notify(getData().id, e);
            System.exit(0);
        } catch (AssertionError e) {
            logger.error("workflow failed", e);
            backchannel.notify(getData().id, e);
            System.exit(0);
        }
    }

    private void delayedResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        dummyResponseSender.foo(cid, 100, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid);
        checkResponse(cid);
    }

    private void earlyResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        dummyResponseSender.foo(cid, 0, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid);
        checkResponse(cid);
    }

    private void checkResponse(final String cid) {
        Response<String> r = getAndRemoveResponse(cid);
        if (r == null) {
            logger.warn("Response is null for wfid=" + getId() + " and cid=" + cid);
        } else {
            String expectedResponse = "foo" + cid;
            if (!expectedResponse.equals(r.getResponse())) {
                logger.warn("Unexpected response for  wfid=" + getId() + " and cid=" + cid + ": expected=" + expectedResponse + " received=" + r.getResponse());
            }
        }
    }

    private void timeoutResponse() throws Interrupt {
        final String cid = getEngine().createUUID();
        wait(WaitMode.ALL, 100, cid);
        Response<String> r = getAndRemoveResponse(cid);
        if (r == null) {
            logger.warn("Response is null for wfid=" + getId() + " and cid=" + cid);
        } else {
            if (!r.isTimeout()) {
                logger.warn("Expected timeout for wfid=" + getId() + " and cid=" + cid);
            }
        }
    }

    private void delayedMultiResponse() throws Interrupt {
        final String cid1 = getEngine().createUUID();
        final String cid2 = getEngine().createUUID();
        final String cid3 = getEngine().createUUID();
        dummyResponseSender.foo(cid1, 50, TimeUnit.MILLISECONDS);
        dummyResponseSender.foo(cid2, 100, TimeUnit.MILLISECONDS);
        dummyResponseSender.foo(cid3, 150, TimeUnit.MILLISECONDS);
        wait(WaitMode.ALL, DEFAULT_TIMEOUT, cid1, cid2, cid3);
        checkResponse(cid1);
        checkResponse(cid2);
        checkResponse(cid3);

    }

}
