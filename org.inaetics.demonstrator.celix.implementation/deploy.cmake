deploy("inaetics-demonstrator-all" BUNDLES
		org.inaetics.demonstrator.api.queue.SampleQueue
        org.inaetics.demonstrator.api.queue.SampleQueue_endpoint
        org.inaetics.demonstrator.api.queue.SampleQueue_proxy
        org.inaetics.demonstrator.api.stats.StatisticTracker
        org.inaetics.demonstrator.api.stats.StatsProvider_endpoint 
        org.inaetics.demonstrator.api.stats.StatsProvider_proxy
        org.inaetics.demonstrator.api.datastore.DataStore
        org.inaetics.demonstrator.api.datastore.DataStore_endpoint
        org.inaetics.demonstrator.api.datastore.DataStore_proxy
        org.inaetics.demonstrator.api.producer.Producer
	org.inaetics.demonstrator.api.producer.Producer_endpoint
        org.inaetics.demonstrator.api.processor.Processor
      )

  deploy("inaetics-demonstrator-queue" BUNDLES
		${CELIX_BUNDLES_DIR}/shell.zip
		${CELIX_BUNDLES_DIR}/shell_tui.zip
		${CELIX_BUNDLES_DIR}/discovery_etcd.zip
		${CELIX_BUNDLES_DIR}/topology_manager.zip
		${CELIX_BUNDLES_DIR}/remote_service_admin_http.zip
		org.inaetics.demonstrator.api.queue.SampleQueue
        ENDPOINTS 
            org.inaetics.demonstrator.api.queue.SampleQueue_endpoint
            org.inaetics.demonstrator.api.stats.StatsProvider_endpoint
        PROPERTIES
            RSA_PORT=50801
            DISCOVERY_CFG_SERVER_PORT=50901
            DISCOVERY_CFG_POLL_ENDPOINTS=http://127.0.0.1:50902/org.apache.celix.discovery.configured, http://127.0.0.1:50903/org.apache.celix.discovery.configured, http://127.0.0.1:50904/org.apache.celix.discovery.configured, http://127.0.0.1:50905/org.apache.celix.discovery.configured, http://127.0.0.1:50906/org.apache.celix.discovery.configured
  )

  deploy("inaetics-demonstrator-statistic-tracker" BUNDLES
		${CELIX_BUNDLES_DIR}/shell.zip
		${CELIX_BUNDLES_DIR}/shell_tui.zip
		${CELIX_BUNDLES_DIR}/topology_manager.zip
		${CELIX_BUNDLES_DIR}/discovery_etcd.zip
		${CELIX_BUNDLES_DIR}/remote_service_admin_http.zip
        org.inaetics.demonstrator.api.stats.StatisticTracker
        ENDPOINTS org.inaetics.demonstrator.api.stats.StatsProvider_proxy
      )

  deploy("inaetics-demonstrator-producer" BUNDLES
		${CELIX_BUNDLES_DIR}/shell.zip
		${CELIX_BUNDLES_DIR}/shell_tui.zip
		${CELIX_BUNDLES_DIR}/topology_manager.zip
		${CELIX_BUNDLES_DIR}/discovery_etcd.zip
		${CELIX_BUNDLES_DIR}/remote_service_admin_http.zip
        org.inaetics.demonstrator.api.producer.Producer
        ENDPOINTS
            org.inaetics.demonstrator.api.queue.SampleQueue_proxy
            org.inaetics.demonstrator.api.stats.StatsProvider_endpoint
	        org.inaetics.demonstrator.api.producer.Producer_endpoint
        PROPERTIES
            RSA_PORT=50802
            DISCOVERY_CFG_SERVER_PORT=50902
            DISCOVERY_CFG_POLL_ENDPOINTS=http://127.0.0.1:50901/org.apache.celix.discovery.configured, http://127.0.0.1:50903/org.apache.celix.discovery.configured, http://127.0.0.1:50904/org.apache.celix.discovery.configured, http://127.0.0.1:50905/org.apache.celix.discovery.configured, http://127.0.0.1:50906/org.apache.celix.discovery.configured
      )

  deploy("inaetics-demonstrator-processor" BUNDLES
		${CELIX_BUNDLES_DIR}/shell.zip
		${CELIX_BUNDLES_DIR}/shell_tui.zip
		${CELIX_BUNDLES_DIR}/topology_manager.zip
        ${CELIX_BUNDLES_DIR}/discovery_etcd.zip
        ${CELIX_BUNDLES_DIR}/remote_service_admin_http.zip
        org.inaetics.demonstrator.api.processor.Processor
        ENDPOINTS 
            org.inaetics.demonstrator.api.queue.SampleQueue_proxy
            org.inaetics.demonstrator.api.datastore.DataStore_proxy
            org.inaetics.demonstrator.api.stats.StatsProvider_endpoint
        PROPERTIES
            RSA_PORT=50803
            DISCOVERY_CFG_SERVER_PORT=50903
            DISCOVERY_CFG_POLL_ENDPOINTS=http://127.0.0.1:50901/org.apache.celix.discovery.configured, http://127.0.0.1:50902/org.apache.celix.discovery.configured, http://127.0.0.1:50904/org.apache.celix.discovery.configured, http://127.0.0.1:50905/org.apache.celix.discovery.configured, http://127.0.0.1:50906/org.apache.celix.discovery.configured
      )
  
  deploy("inaetics-demonstrator-dataStore" BUNDLES
        ${CELIX_BUNDLES_DIR}/shell.zip
        ${CELIX_BUNDLES_DIR}/shell_tui.zip
        ${CELIX_BUNDLES_DIR}/discovery_etcd.zip
        ${CELIX_BUNDLES_DIR}/topology_manager.zip
        ${CELIX_BUNDLES_DIR}/remote_service_admin_http.zip
        org.inaetics.demonstrator.api.datastore.DataStore
        ENDPOINTS 
            org.inaetics.demonstrator.api.datastore.DataStore_endpoint
            org.inaetics.demonstrator.api.stats.StatsProvider_endpoint
        PROPERTIES
            RSA_PORT=50804
            DISCOVERY_CFG_SERVER_PORT=50904
            DISCOVERY_CFG_POLL_ENDPOINTS=http://127.0.0.1:50901/org.apache.celix.discovery.configured, http://127.0.0.1:50902/org.apache.celix.discovery.configured, http://127.0.0.1:50903/org.apache.celix.discovery.configured, http://127.0.0.1:50905/org.apache.celix.discovery.configured, http://127.0.0.1:50906/org.apache.celix.discovery.configured
      )
