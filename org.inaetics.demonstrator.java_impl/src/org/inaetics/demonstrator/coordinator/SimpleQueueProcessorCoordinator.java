/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */
package org.inaetics.demonstrator.coordinator;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.felix.dm.Component;
import org.inaetics.demonstrator.api.stats.StatsProvider;
import org.inaetics.demonstrator.k8sclient.KubernetesClient;
import org.inaetics.demonstrator.k8sclient.ReplicationController;
import org.osgi.service.log.LogService;

public class SimpleQueueProcessorCoordinator {

	private volatile StatsProvider m_queueStatsProvider;
	private volatile LogService m_log;

	private volatile ScheduledExecutorService m_executor;
	private volatile Future<?> m_pollFuture;

	private volatile KubernetesClient m_kubernetesClient;
	private volatile CoordinatorConfig m_config;
	
	public SimpleQueueProcessorCoordinator(CoordinatorConfig config) {
		m_config = config;
		m_executor = Executors.newSingleThreadScheduledExecutor();
	}

	protected final void start(Component comp) {
		
		m_log.log(LogService.LOG_INFO, "using config: " + m_config);

		m_pollFuture = m_executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				checkQueue();
			}
		}, 1, m_config.getPollInterval(), TimeUnit.SECONDS);
	}

	protected final void stop(Component comp) {
		try {
			if (m_pollFuture != null) {
				m_pollFuture.cancel(true);
				m_pollFuture = null;
			}
		} finally {
			m_executor.shutdownNow();
			m_executor = null;
		}
	}

	private void checkQueue() {
		if (m_queueStatsProvider != null) {
			double value = m_queueStatsProvider.getValue();
			m_log.log(LogService.LOG_INFO, "queue utilzation: " + value);
			if (value < m_config.getQueueLowBarrier()) {
				m_log.log(LogService.LOG_INFO, "queue under low barrier, decreasing nr of processors");
				decreaseProcessorCount();
			}
			else if (value > m_config.getQueueHighBarrier()) {
				m_log.log(LogService.LOG_INFO, "queue over high barrier, increasing nr of processors");
				increaseProcessorCount();
			}
		}
	}
	
	private void increaseProcessorCount() {
		ReplicationController felixProcessorController = getFelixProcessorController();
		ReplicationController celixProcessorController = getCelixProcessorController();
		if (felixProcessorController == null || celixProcessorController == null) {
			m_log.log(LogService.LOG_INFO, "didn't get rc!");
			return;
		}
		if (celixProcessorController.getReplicaCount() <= felixProcessorController.getReplicaCount()) {
			celixProcessorController.increaseReplicaCount(m_config.getMaxNrProcessors() / 2);
			m_kubernetesClient.updateReplicationController(celixProcessorController);
		}
		else {
			felixProcessorController.increaseReplicaCount(m_config.getMaxNrProcessors() / 2);
			m_kubernetesClient.updateReplicationController(felixProcessorController);
		}
	}

	private void decreaseProcessorCount() {
		ReplicationController felixProcessorController = getFelixProcessorController();
		ReplicationController celixProcessorController = getCelixProcessorController();
		if (felixProcessorController == null || celixProcessorController == null) {
			m_log.log(LogService.LOG_INFO, "didn't get rc!");
			return;
		}
		if (celixProcessorController.getReplicaCount() > felixProcessorController.getReplicaCount()) {
			celixProcessorController.decreaseReplicaCount(1);
			m_kubernetesClient.updateReplicationController(celixProcessorController);
		}
		else {
			felixProcessorController.decreaseReplicaCount(0);
			m_kubernetesClient.updateReplicationController(felixProcessorController);
		}
	}
	
	private ReplicationController getFelixProcessorController() {
		ReplicationController processorController = m_kubernetesClient.getReplicationController(
				m_config.getFelixProcessorControllerName());
		return processorController;
	}
	
	private ReplicationController getCelixProcessorController() {
		ReplicationController processorController = m_kubernetesClient.getReplicationController(
				m_config.getCelixProcessorControllerName());
		return processorController;
	}
	
}
