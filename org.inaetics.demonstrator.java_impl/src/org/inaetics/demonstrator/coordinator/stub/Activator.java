/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */
package org.inaetics.demonstrator.coordinator.stub;

import java.util.Properties;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.inaetics.demonstrator.api.coordinator.CoordinatorService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

public class Activator extends DependencyActivatorBase {
    public static final String CONFIG_PID = "org.inaetics.demonstrator.coordinator.stub";

    @Override
    public void init(BundleContext context, DependencyManager manager) throws Exception {
        String[] ifaces = { ManagedService.class.getName(), CoordinatorService.class.getName() };
        
        Properties props = new Properties();
        props.put(Constants.SERVICE_PID, CONFIG_PID);

        manager.add(createComponent()
            .setInterface(ifaces, props)
            .setImplementation(StubCoordinatorService.class)
            .add(createServiceDependency().setService(LogService.class).setRequired(false)));
    }
}
