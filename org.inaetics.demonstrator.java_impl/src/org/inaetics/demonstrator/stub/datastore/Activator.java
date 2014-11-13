/*
 * Copyright (c) 2014 INAETICS, <www.inaetics.org>
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
package org.inaetics.demonstrator.stub.datastore;

import java.util.Properties;

import javax.servlet.Servlet;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.inaetics.demonstrator.api.datastore.DataStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;
import org.osgi.service.remoteserviceadmin.RemoteConstants;

public class Activator extends DependencyActivatorBase {
	private static final String PID = "InMemoryDataStore";

	@Override
	public void init(BundleContext context, DependencyManager manager) throws Exception {
		String[] ifaces = { DataStore.class.getName(), Servlet.class.getName(), ManagedService.class.getName() };

		Properties props = new Properties();
		props.put("alias", "/datastore");
		props.put(Constants.SERVICE_PID, PID);
		props.put(RemoteConstants.SERVICE_EXPORTED_INTERFACES, ifaces[0]);
		props.put("type", "memory");

		manager.add(createComponent()
			.setInterface(ifaces, props)
			.setImplementation(InMemoryDataStore.class)
			.add(createServiceDependency().setService(LogService.class).setRequired(false))
		);
	}
}
