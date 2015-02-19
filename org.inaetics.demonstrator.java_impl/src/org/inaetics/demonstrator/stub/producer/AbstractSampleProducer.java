/**
 * Licensed under Apache License v2. See LICENSE for more information.
 */
package org.inaetics.demonstrator.stub.producer;

import java.util.Dictionary;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.felix.dm.Component;
import org.inaetics.demonstrator.api.producer.Producer;
import org.inaetics.demonstrator.api.stats.StatsProvider;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

/**
 * Abstract base implementation of a sample {@link Producer}.
 */
public abstract class AbstractSampleProducer implements Producer, StatsProvider, ManagedService {
    private final Random m_rnd;
    private final String m_name;
    private final long m_taskInterval;

    private ScheduledExecutorService m_executor;
    private ScheduledFuture<?> m_generatorFuture;
    private ScheduledFuture<?> m_samplerFuture;

    private volatile double m_producedAvg = 0;
    // Injected by Felix DM...
    private volatile Component m_component;
    private volatile LogService m_log;

    protected AbstractSampleProducer(String name, long taskInterval) {
        m_rnd = new Random();
        m_name = name;
        m_taskInterval = taskInterval;
    }

    @Override
    public String getMeasurementUnit() {
        return "samples/sec";
    }

    @Override
    public final String getName() {
        Long id = (Long) m_component.getServiceRegistration().getReference().getProperty(Constants.SERVICE_ID);
        return String.format("%s #%d", m_name, id);
    }

    @Override
    public String getType() {
        return "throughput";
    }

    @Override
    public double getValue() {
        return m_producedAvg;
    }

    @Override
    public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
        // Nothing yet...
    }

    /**
     * @param time the time (in milliseconds) this producer is running.
     * @return the throughput of this producer.
     */
    protected abstract double calculateThroughput(long time);

    protected final void info(String msg, Object... args) {
        m_log.log(LogService.LOG_INFO, String.format(msg, args));
    }

    protected final void msleep(long time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Implement this to produce the actual sample(s).s
     */
    protected abstract void produceSamples();

    protected final double randomSampleValue() {
        return (m_rnd.nextDouble() * 200.0) - 100.0;
    }

    /**
     * Called by Felix DM when starting this component.
     */
    protected final void start() throws Exception {
        m_executor = Executors.newScheduledThreadPool(2);

        m_generatorFuture = m_executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    produceSamples();
                } catch (Exception e) {
                    // Ignore, not much we can do about this...
                    info("Failed to produce sample(s)! Cause: %s",
                        (e.getMessage() == null ? "NullPointerException" : e.getMessage()));
                    m_log.log(LogService.LOG_DEBUG, "Failed to produce sample(s)!", e);
                }
            }
        }, 250, m_taskInterval, TimeUnit.MILLISECONDS);

        m_samplerFuture = m_executor.scheduleAtFixedRate(new Runnable() {
            private final long m_startTime = System.currentTimeMillis();

            @Override
            public void run() {
                try {
                    m_producedAvg = calculateThroughput(System.currentTimeMillis() - m_startTime);
                } catch (Exception e) {
                    m_log.log(LogService.LOG_DEBUG, "Failed to calculate average throughput!", e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Called by Felix DM when stopping this component.
     */
    protected final void stop() throws Exception {
        if (m_generatorFuture != null) {
            m_generatorFuture.cancel(true);
            m_generatorFuture = null;
        }
        if (m_samplerFuture != null) {
            m_samplerFuture.cancel(true);
            m_samplerFuture = null;
        }
        m_executor.shutdown();
        m_executor.awaitTermination(10, TimeUnit.SECONDS);
    }
}