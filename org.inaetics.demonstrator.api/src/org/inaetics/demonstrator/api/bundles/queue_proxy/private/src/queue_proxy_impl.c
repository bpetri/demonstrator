#include <jansson.h>

#include <string.h>
#include <stddef.h>

#include "celix_errno.h"
#include "array_list.h"
#include "queue_proxy_impl.h"

#include "inaetics_demonstrator_api/sample_queue.h"


/* Allows the use of Jansson < 2.3 */
#ifndef JSON_DECODE_ANY
#define JSON_DECODE_ANY 0
#endif

celix_status_t queueProxy_setEndpointDescription(void *proxy, endpoint_description_pt endpoint);
celix_status_t queueProxy_setHandler(void *proxy, void *handler);
celix_status_t queueProxy_setCallback(void *proxy, sendToHandle callback);


celix_status_t queueProxy_create(bundle_context_pt context, sample_queue_type **queue)  {
	celix_status_t status = CELIX_SUCCESS;
	*queue = calloc(1, sizeof(**queue));
	if (!*queue) {
		status = CELIX_ENOMEM;
	} else {
		(*queue)->context = context;
		(*queue)->endpoint = NULL;
		(*queue)->sendToCallback=NULL;
		(*queue)->sendToHandler=NULL;
	}

	return status;
}

// { "m": "" "a":["arg1", "arg2"] }
celix_status_t queueProxy_put(sample_queue_type* queue, struct sample workSample, bool *sampleTaken) {
	celix_status_t status = CELIX_SUCCESS;

	if (queue->endpoint != NULL) {
		json_t *root;

		root = json_pack("{s:s, s:[Iff]}", "m", "put(DD)D", "d", workSample.time, workSample.value1, workSample.value2);

		char *data = json_dumps(root, 0);
		char *reply = malloc(256);
		int replyStatus = 0;

		status = queue->sendToCallback(queue->sendToHandler, queue->endpoint, data, &reply, &replyStatus);

		if (status == CELIX_SUCCESS) {
		    printf("Handle reply: %s\n", reply);
			json_error_t error;
			json_t *js_reply = json_loads(reply, 0, &error);
			if (js_reply) {
				json_unpack(js_reply, "{s:b}", "r", sampleTaken);
			} else {
				printf("PROXY: got error '%s' for '%s'\n", error.text, reply);
				status = CELIX_BUNDLE_EXCEPTION;
			}
		}

		free(data);
		free(reply);
	} else {
		printf("QUEUE_PROXY: No endpoint information available\n");
		status = CELIX_BUNDLE_EXCEPTION;
	}
}


celix_status_t queueProxy_putAll(sample_queue_type *queue, struct sample *samples, uint32_t size, uint32_t *samplesTaken)
{
	celix_status_t status = CELIX_SUCCESS;

	if (queue->endpoint != NULL) {
		uint32_t arrayCnt = 0;
		json_t *arrayRoot;
		json_t *root;


		json_t *array = json_array();

		for (arrayCnt = 0; arrayCnt < size; arrayCnt++) {
			json_t *element = json_pack("{s:[Iff]}", "d", samples[arrayCnt].time, samples[arrayCnt].value1, samples[arrayCnt].value2);
			json_array_append(array, element);
		}

		root = json_pack("{s:s, s:o}", "m", "putAll(DD)D", "d", array);

		char *data = json_dumps(root, 0);

		printf("data is %s\n", data);

		char *reply = malloc(256);
		int replyStatus = 0;

		status = queue->sendToCallback(queue->sendToHandler, queue->endpoint, data, &reply, &replyStatus);

		if (status == CELIX_SUCCESS) {
		    printf("Handle reply: %s\n", reply);
			json_error_t error;
			json_t *js_reply = json_loads(reply, 0, &error);
			if (js_reply) {
				json_unpack(js_reply, "{s:i}", "r", samplesTaken);
			} else {
				printf("PROXY: got error '%s' for '%s'\n", error.text, reply);
				status = CELIX_BUNDLE_EXCEPTION;
			}
		}

		free(data);
		free(reply);
	} else {
		printf("QUEUE_PROXY: No endpoint information available\n");
		status = CELIX_BUNDLE_EXCEPTION;
	}
}





celix_status_t queueProxy_registerProxyService(void* proxyFactoryService, endpoint_description_pt endpointDescription, void* rsa, sendToHandle sendToCallback) {
	celix_status_t status = CELIX_SUCCESS;

	remote_proxy_factory_service_pt queueProxyFactoryService = (remote_proxy_factory_service_pt) proxyFactoryService;
	sample_queue_type* queue = NULL;
	struct sample_queue_service* queueService = NULL;

	queueProxy_create(queueProxyFactoryService->context, &queue);
	queueService = calloc(1, sizeof(*queueService));
	queueService->sampleQueue = queue;
	queueService->put = queueProxy_put;
	queueService->putAll = queueProxy_putAll;
	queueService->take = NULL;
	queueService->takeAll = NULL;

	properties_pt srvcProps = properties_create();
	properties_set(srvcProps, (char *) "proxy.interface", (char *) INAETICS_DEMONSTATOR_API__SAMPLE_QUEUE_SERVICE_NAME);
	properties_set(srvcProps, (char *) "endpoint.framework.uuid", (char *) endpointDescription->frameworkUUID);

	service_registration_pt proxyReg = NULL;

	queueProxy_setEndpointDescription(queue, endpointDescription);
	queueProxy_setHandler(queue, rsa);
	queueProxy_setCallback(queue, sendToCallback);

	if (bundleContext_registerService(queueProxyFactoryService->context, INAETICS_DEMONSTATOR_API__SAMPLE_QUEUE_SERVICE_NAME, queueService, srvcProps, &proxyReg) != CELIX_SUCCESS)
	{
		printf("QUEUE_PROXY: error while registering queue service\n");
	}

	hashMap_put(queueProxyFactoryService->proxy_registrations, endpointDescription, proxyReg);


	return status;
}


celix_status_t queueProxy_unregisterProxyService(void* proxyFactoryService, endpoint_description_pt endpointDescription) {
	celix_status_t status = CELIX_SUCCESS;

	remote_proxy_factory_service_pt queueProxyFactoryService = (remote_proxy_factory_service_pt) proxyFactoryService;
	service_registration_pt proxyReg = hashMap_get(queueProxyFactoryService->proxy_registrations, endpointDescription);

	if (proxyReg != NULL)
	{
		serviceRegistration_unregister(proxyReg);
	}

	return status;
}


celix_status_t queueProxy_setEndpointDescription(void *proxy, endpoint_description_pt endpoint) {
	celix_status_t status = CELIX_SUCCESS;

	sample_queue_type* queue = proxy;
	queue->endpoint = endpoint;

	return status;
}


celix_status_t queueProxy_setHandler(void *proxy, void *handler) {
	celix_status_t status = CELIX_SUCCESS;

	sample_queue_type* queue = proxy;
	queue->sendToHandler = handler;

	return status;
}


celix_status_t queueProxy_setCallback(void *proxy, sendToHandle callback) {
	celix_status_t status = CELIX_SUCCESS;

	sample_queue_type* queue = proxy;
	queue->sendToCallback = callback;

	return status;
}

