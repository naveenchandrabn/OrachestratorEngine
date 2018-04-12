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
package org.copperengine.JBossDataGrid;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.NullArgumentException;
import org.copperengine.core.CopperRuntimeException;
import org.copperengine.core.ProcessingState;
import org.copperengine.core.WaitMode;
import org.copperengine.core.monitoring.RuntimeStatisticsCollector;
import org.copperengine.core.persistent.SerializedWorkflow;
import org.copperengine.core.persistent.hybrid.HybridDBStorageAccessor;
import org.copperengine.core.persistent.hybrid.Storage;
import org.copperengine.core.persistent.hybrid.WorkflowInstance;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.Cache;
import org.infinispan.CacheSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Implementation of the {@link Storage} interface backed by a Apache Cassandra
 * DB.
 * 
 * @author austermann
 *
 */
public class JBossDataStorage implements Storage {

	private static final Logger logger = LoggerFactory.getLogger(JBossDataStorage.class);

	private final Executor executor;
	private final JsonMapper jsonMapper = new JsonMapperImpl();
	private final RuntimeStatisticsCollector runtimeStatisticsCollector;
	private int ttlEarlyResponseSeconds = 1 * 24 * 60 * 60; // one day
	private int initializationTimeoutSeconds = 1 * 24 * 60 * 60; // one day
	private DefaultCacheManager sessionManager;

	public JBossDataStorage(final DefaultCacheManager sessionManager, final Executor executor,
			final RuntimeStatisticsCollector runtimeStatisticsCollector) {
		if (sessionManager == null)
			throw new NullArgumentException("sessionManager");

		if (executor == null)
			throw new NullArgumentException("executor");

		if (runtimeStatisticsCollector == null)
			throw new NullArgumentException("runtimeStatisticsCollector");

		this.executor = executor;
		this.runtimeStatisticsCollector = runtimeStatisticsCollector;
		this.sessionManager = sessionManager;

	}

	public void setTtlEarlyResponseSeconds(int ttlEarlyResponseSeconds) {
		if (ttlEarlyResponseSeconds <= 0)
			throw new IllegalArgumentException();
		this.ttlEarlyResponseSeconds = ttlEarlyResponseSeconds;
	}

	public void setInitializationTimeoutSeconds(int initializationTimeoutSeconds) {
		if (initializationTimeoutSeconds <= 0)
			throw new IllegalArgumentException();
		this.initializationTimeoutSeconds = initializationTimeoutSeconds;
	}

	@Override
	public void safeWorkflowInstance(final WorkflowInstance cw, final boolean initialInsert) throws Exception {
		logger.debug("safeWorkflow({})", cw);
		new JbossCacheOperation<Void>(logger) {
			@Override
			protected Void execute() throws Exception {
				if (initialInsert) {
					final long startTS = System.nanoTime();
					Cache<String, String> c = getCacheCopWFIId();
					c.put(cw.id, cw.id);
					runtimeStatisticsCollector.submit("wfii.ins", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
				}
				if (cw.cid2ResponseMap == null || cw.cid2ResponseMap.isEmpty()) {
					final long startTS = System.nanoTime();
					Cache<String, WorkflowInstance> c = getCacheCopWorkflowInstance();
					c.put(cw.id, cw);
					runtimeStatisticsCollector.submit("wfi.update.nowait", 1, System.nanoTime() - startTS,
							TimeUnit.NANOSECONDS);
				} else {
					final long startTS = System.nanoTime();
					Cache<String, WorkflowInstance> c = getCacheCopWorkflowInstance();
					c.put(cw.id, cw);
					runtimeStatisticsCollector.submit("wfi.update.wait", 1, System.nanoTime() - startTS,
							TimeUnit.NANOSECONDS);
				}

				return null;
			}
		}.run();
	}

	@Override
	public ListenableFuture<Void> deleteWorkflowInstance(String wfId) throws Exception {
		logger.debug("deleteWorkflowInstance({})", wfId);
		final long startTS = System.nanoTime();
		Cache<String, String> cCopWFIId = getCacheCopWFIId();
		cCopWFIId.removeAsync(wfId);
		Cache<String, WorkflowInstance> cCopWorkflowInstance = getCacheCopWorkflowInstance();
		cCopWorkflowInstance.removeAsync(wfId);

		return createSettableFuture("wfi.delete", startTS);
	}

	private SettableFuture<Void> createSettableFuture(final String mpId, final long startTsNanos) {
		final SettableFuture<Void> rv = SettableFuture.create();

		runtimeStatisticsCollector.submit(mpId, 1, System.nanoTime() - startTsNanos, TimeUnit.NANOSECONDS);

		return rv;
	}

	@Override
	public WorkflowInstance readWorkflowInstance(final String wfId) throws Exception {
		logger.debug("readCassandraWorkflow({})", wfId);
		return new JbossCacheOperation<WorkflowInstance>(logger) {
			@Override
			protected WorkflowInstance execute() throws Exception {
				final long startTS = System.nanoTime();
				Cache<String, WorkflowInstance> cCopWorkflowInstance = getCacheCopWorkflowInstance();
				final WorkflowInstance cw = cCopWorkflowInstance.get(wfId);
				runtimeStatisticsCollector.submit("wfi.read", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
				return cw;
			}
		}.run();
	}

	@Override
	public ListenableFuture<Void> safeEarlyResponse(String correlationId, String serializedResponse) throws Exception {
		logger.debug("safeEarlyResponse({})", correlationId);
		final long startTS = System.nanoTime();
		Cache<String, String> cCopEarlyResponse = getCacheCopEarlyResponse();
		cCopEarlyResponse.put(correlationId, serializedResponse, ttlEarlyResponseSeconds, TimeUnit.MILLISECONDS);
		return createSettableFuture("ear.insert", startTS);
	}

	@Override
	public String readEarlyResponse(final String correlationId) throws Exception {
		logger.debug("readEarlyResponse({})", correlationId);
		return new JbossCacheOperation<String>(logger) {
			@Override
			protected String execute() throws Exception {
				final long startTS = System.nanoTime();
				runtimeStatisticsCollector.submit("ear.read", 1, System.nanoTime() - startTS, TimeUnit.NANOSECONDS);
				Cache<String, String> cCopEarlyResponse = getCacheCopEarlyResponse();
				return cCopEarlyResponse.get(correlationId);
			}
		}.run();
	}

	@Override
	public ListenableFuture<Void> deleteEarlyResponse(String correlationId) throws Exception {
		logger.debug("deleteEarlyResponse({})", correlationId);
		final long startTS = System.nanoTime();
		Cache<String, String> cCopEarlyResponse = getCacheCopEarlyResponse();
		cCopEarlyResponse.removeAsync(correlationId);
		return createSettableFuture("ear.delete", startTS);
	}

	@Override
	public void initialize(final HybridDBStorageAccessor internalStorageAccessor, int numberOfThreads)
			throws Exception {

		// TODO instead of blocking the startup until all active workflow instances are
		// read and resumed, it is
		// sufficient to read just their existing IDs in COP_WFI_ID and resume them in
		// the background while already
		// starting the engine an accepting new instances.

		if (numberOfThreads <= 0)
			numberOfThreads = 1;
		logger.info("Starting to initialize with {} threads ...", numberOfThreads);
		final ExecutorService execService = Executors.newFixedThreadPool(numberOfThreads);
		final long startTS = System.currentTimeMillis();
		// final ResultSet rs =
		// session.execute(preparedStatements.get(CQL_SEL_WFI_ID_ALL).bind().setFetchSize(500).setConsistencyLevel(ConsistencyLevel.ONE));
		CacheSet<Entry<String, String>> values = getCacheCopWFIId().entrySet();
		int counter = 0;
		for (Entry<String, String> val : values) {
			execService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						resume(val.getKey(), internalStorageAccessor);
					} catch (Exception e) {
						logger.error("resume failed", e);
					}
				}
			});
		}

		logger.info("Read {} IDs in {} msec", counter, System.currentTimeMillis() - startTS);
		execService.shutdown();
		final boolean timeoutHappened = !execService.awaitTermination(initializationTimeoutSeconds, TimeUnit.SECONDS);
		if (timeoutHappened) {
			throw new CopperRuntimeException("initialize timed out!");
		}
		logger.info("Finished initialization - read {} rows in {} msec", counter, System.currentTimeMillis() - startTS);
		runtimeStatisticsCollector.submit("storage.init", counter, System.currentTimeMillis() - startTS,
				TimeUnit.MILLISECONDS);
	}

	private void resume(final String wfId, final HybridDBStorageAccessor internalStorageAccessor) throws Exception {
		logger.trace("resume(wfId={})", wfId);
		WorkflowInstance cw = getCacheCopWorkflowInstance().get(wfId);
		if (cw == null) {
			logger.warn("No workflow instance {} found - deleting row in COP_WFI_ID", wfId);
			getCacheCopWFIId().removeAsync(wfId);
			return;
		}

		final String ppoolId = cw.ppoolId;
		final int prio = cw.prio;
		final WaitMode waitMode = cw.waitMode;
		final Map<String, String> responseMap = cw.cid2ResponseMap;
		final ProcessingState state = cw.state;
		final Date timeout = cw.timeout;
		final boolean timeoutOccured = timeout != null && timeout.getTime() <= System.currentTimeMillis();

		if (state == ProcessingState.ERROR || state == ProcessingState.INVALID) {
			return;
		}

		if (state == ProcessingState.ENQUEUED) {
			internalStorageAccessor.enqueue(wfId, ppoolId, prio);
			return;
		}

		if (responseMap != null) {
			final List<String> missingResponseCorrelationIds = new ArrayList<String>();
			int numberOfAvailableResponses = 0;
			for (Entry<String, String> e : responseMap.entrySet()) {
				final String correlationId = e.getKey();
				final String response = e.getValue();
				internalStorageAccessor.registerCorrelationId(correlationId, wfId);
				if (response != null) {
					numberOfAvailableResponses++;
				} else {
					missingResponseCorrelationIds.add(correlationId);
				}
			}
			boolean modified = false;
			if (!missingResponseCorrelationIds.isEmpty()) {
				// check for early responses
				for (String cid : missingResponseCorrelationIds) {
					String earlyResponse = readEarlyResponse(cid);
					if (earlyResponse != null) {
						responseMap.put(cid, earlyResponse);
						numberOfAvailableResponses++;
						modified = true;
					}
				}
			}
			if (modified || timeoutOccured) {
				final ProcessingState newState = (timeoutOccured || numberOfAvailableResponses == responseMap.size()
						|| (numberOfAvailableResponses == 1 && waitMode == WaitMode.FIRST)) ? ProcessingState.ENQUEUED
								: ProcessingState.WAITING;
				final String responseMapJson = jsonMapper.toJSON(responseMap);
				WorkflowInstance cwMod = getCacheCopWorkflowInstance().get(wfId);
				if (cwMod != null) {
					cwMod.state = newState;
					cwMod.cid2ResponseMap = responseMap;
				}
				if (newState == ProcessingState.ENQUEUED) {
					internalStorageAccessor.enqueue(wfId, ppoolId, prio);
				}
			}

		}
	}

	@Override
	public ListenableFuture<Void> updateWorkflowInstanceState(final String wfId, final ProcessingState state)
			throws Exception {
		logger.debug("updateWorkflowInstanceState({}, {})", wfId, state);
		final long startTS = System.nanoTime();
		WorkflowInstance cw = getCacheCopWorkflowInstance().get(wfId);
		if (cw != null) {
			cw.state = state;
		}
		return createSettableFuture("wfi.update.state", startTS);
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> toResponseMap(String v) {
		return v == null ? null : jsonMapper.fromJSON(v, HashMap.class);
	}

	private WaitMode toWaitMode(String v) {
		return v == null ? null : WaitMode.valueOf(v);
	}

	public Cache<String, String> getCacheCopWFIId() {
		Cache<String, String> c = sessionManager.getCache("cop_wfi_id");
		return c;
	}

	public Cache<String, WorkflowInstance> getCacheCopWorkflowInstance() {
		Cache<String, WorkflowInstance> c = sessionManager.getCache("cop_workflow_instance");
		return c;
	}

	public Cache<String, String> getCacheCopEarlyResponse() {
		Cache<String, String> c = sessionManager.getCache("cop_early_response");
		return c;
	}

}
