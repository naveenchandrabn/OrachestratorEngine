package org.copperengine.JBossDataGrid;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.CacheMode;

public class JBossCacheStorage {
	
	private static final long ENTRY_LIFESPAN = 600 * 1000; // 600 seconds

	private static DefaultCacheManager manager;

	static DefaultCacheManager getCacheManager() {
		if (manager == null) {
			GlobalConfiguration glob = new GlobalConfigurationBuilder().clusteredDefault() // Builds a default clustered
					// configuration
					.globalJmxStatistics().allowDuplicateDomains(true).enable() // This method enables the jmx
																				// statistics of
					// the global configuration and allows for duplicate JMX domains
					.build(); // Builds the GlobalConfiguration object
			Configuration loc = new ConfigurationBuilder().jmxStatistics().enable() // Enable JMX statistics
					.clustering().cacheMode(CacheMode.DIST_SYNC) // Set Cache mode to DISTRIBUTED with SYNCHRONOUS
																	// replication
					.hash().numOwners(2) // Keeps two copies of each key/value pair
					.expiration().lifespan(ENTRY_LIFESPAN) // Set expiration - cache entries expire after some time
															// (given
															// by
					// the lifespan parameter) and are removed from the cache (cluster-wide).
					.build();
			manager = new DefaultCacheManager(glob, loc, true);
		}
		return manager;
	}

	public static void cleanUp() {
		manager.stop();
		manager = null;
	}
	
	

}
