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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.copperengine.core.DependencyInjector;
import org.copperengine.core.persistent.PersistentScottyEngine;
import org.copperengine.core.persistent.hybrid.HybridEngineFactory;
import org.copperengine.core.persistent.hybrid.Storage;
import org.slf4j.Logger;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Utility class to create a {@link PersistentScottyEngine} using a cassandra cluster as underlying storage.
 * <p>
 * Usage is quite simple, e.g. using a SupplierDependencyInjector:
 * 
 * <pre>
 * CassandraEngineFactory&lt;SupplierDependencyInjector&gt; engineFactory = new CassandraEngineFactory&lt;SupplierDependencyInjector&gt;(Arrays.asList(&quot;package.of.copper.workflow.classes&quot;)) {
 *     &#064;Override
 *     protected SupplierDependencyInjector createDependencyInjector() {
 *         return new SupplierDependencyInjector();
 *     }
 * };
 * engineFactory.getEngine().startup();
 * </pre>
 * 
 * @author austermann
 *
 * @param <T>
 *        type of DependencyInjector to be used from the created engine
 */
public abstract class JBossDataGridEngineFactory<T extends DependencyInjector> extends HybridEngineFactory<T> {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JBossDataGridEngineFactory.class);

	protected final Supplier<ScheduledExecutorService> scheduledExecutorService;

	public JBossDataGridEngineFactory(List<String> wfPackges) {
		super(wfPackges);

		scheduledExecutorService = Suppliers.memoize(new Supplier<ScheduledExecutorService>() {
			@Override
			public ScheduledExecutorService get() {
				logger.info("Creating ScheduledExecutorService...");
				return createScheduledExecutorService();
			}
		});
	}

	protected ScheduledExecutorService createScheduledExecutorService() {
		return Executors.newScheduledThreadPool(2);
	}

	protected Storage createStorage() {
		final JBossDataStorage cs = new JBossDataStorage(JBossCacheStorage.getCacheManager(), executorService.get(),
				statisticCollector.get());
		return cs;

	}

	public void destroyEngine() {
		super.destroyEngine();

		JBossCacheStorage.cleanUp();

		scheduledExecutorService.get().shutdown();
	}

}
