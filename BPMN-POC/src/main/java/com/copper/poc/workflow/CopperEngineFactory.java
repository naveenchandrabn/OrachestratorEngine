/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.copper.poc.workflow;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.Arrays;
import org.copperengine.core.persistent.cassandra.CassandraSessionManager;
import org.copperengine.core.util.Backchannel;
import org.copperengine.core.util.BackchannelDefaultImpl;
import org.copperengine.core.util.PojoDependencyInjector;
import org.copperengine.ext.util.Supplier2Provider;

/**
 *
 * @author jaja0617
 */
public class CopperEngineFactory extends org.copperengine.core.persistent.cassandra.CassandraEngineFactory<PojoDependencyInjector> {

    public final Supplier<Backchannel> backchannel;
    public final Supplier<DummyResponseSender> dummyResponseSender;
    protected final boolean truncate = false;

    public CopperEngineFactory() {
        super(Arrays.asList("com.copper.poc.workflow.dir"));
        super.setCassandraHosts(Arrays.asList("localhost"));

        backchannel = Suppliers.memoize(new Supplier<Backchannel>() {
            @Override
            public Backchannel get() {
                return new BackchannelDefaultImpl();
            }
        });
        dummyResponseSender = Suppliers.memoize(new Supplier<DummyResponseSender>() {
            @Override
            public DummyResponseSender get() {
                return new DummyResponseSender(scheduledExecutorService.get(), engine.get());
            }
        });
        dependencyInjector.get().register("dummyResponseSender", new Supplier2Provider<>(dummyResponseSender));
        dependencyInjector.get().register("backchannel", new Supplier2Provider<>(backchannel));
    }

    @Override
    protected CassandraSessionManager createCassandraSessionManager() {
        final CassandraSessionManager csm = super.createCassandraSessionManager();
        if (truncate) {
            csm.getSession().execute("truncate COP_WORKFLOW_INSTANCE");
            csm.getSession().execute("truncate COP_EARLY_RESPONSE");
            csm.getSession().execute("truncate COP_WFI_ID");
        }
        return csm;
    }

    @Override
    protected PojoDependencyInjector createDependencyInjector() {
        return new PojoDependencyInjector();
    }

    public Backchannel getBackchannel() {
        return backchannel.get();
    }

}
