/*
 * Copyright (c) 2015 Technische Universität Berlin
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nubomedia.qosmanager.beans.openbaton;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.nubomedia.qosmanager.openbaton.OpenbatonEvent;
import org.nubomedia.qosmanager.utils.ConfigReader;
import org.nubomedia.qosmanager.utils.ConfigurationBeans;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.catalogue.nfvo.EndpointType;
import org.openbaton.catalogue.nfvo.EventEndpoint;
import org.openbaton.sdk.NFVORequestor;
import org.openbaton.sdk.api.exception.SDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by maa on 11.11.15.
 */
@Service
public class OpenbatonEventSubscription {

    private NFVORequestor requestor;
    private Logger logger;
    private Properties properties;
    @Autowired
    private QoSAllocator creator;
    @Autowired
    private Gson mapper;
    private List<String> eventIds;


    @PostConstruct
    private void init() throws SDKException, IOException {

        this.properties = ConfigReader.readProperties();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.requestor = new NFVORequestor("", "", properties.getProperty("nfvo.baseURL"), properties.getProperty("nfvo.basePort"), "1");
        this.eventIds = new ArrayList<>();

        EventEndpoint eventEndpointCreation = new EventEndpoint();
        eventEndpointCreation.setType(EndpointType.RABBIT);
        eventEndpointCreation.setEvent(Action.INSTANTIATE_FINISH);
        eventEndpointCreation.setEndpoint(ConfigurationBeans.queueName_eventInstatiateFinish);
        eventEndpointCreation.setName("eventNSRCreated");
        eventEndpointCreation = requestor.getEventAgent().create(eventEndpointCreation);

        EventEndpoint eventEndpointDeletion = new EventEndpoint();
        eventEndpointDeletion.setType(EndpointType.RABBIT);
        eventEndpointDeletion.setEvent(Action.RELEASE_RESOURCES_FINISH);
        eventEndpointDeletion.setEndpoint(ConfigurationBeans.queueName_eventResourcesReleaseFinish);
        eventEndpointDeletion.setName("eventNSRCreated");
        eventEndpointDeletion = requestor.getEventAgent().create(eventEndpointDeletion);

        this.eventIds.add(eventEndpointCreation.getId());
        this.eventIds.add(eventEndpointDeletion.getId());

    }

    public void receiveNewNsr(String message) {
        logger.info("received new event " + message);
        OpenbatonEvent evt;

        try {
            logger.debug("Trying to deserialize it");
            evt = mapper.fromJson(message, OpenbatonEvent.class);
        } catch (JsonParseException e) {
            if (logger.isDebugEnabled() || logger.isTraceEnabled())
                logger.warn("Error in payload, expected NSR ", e);
            else
                logger.warn("Error in payload, expected NSR " + e.getMessage());
            return;
        }

        logger.debug("Deserialized!!!");
        logger.debug("ACTION: " + evt.getAction() + " PAYLOAD: " + evt.getPayload().toString());
        NetworkServiceRecord nsr = evt.getPayload();
        logger.debug("NSR is " + nsr.toString());

        vnfrloop:
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
            logger.debug("VNFR: " + vnfr.toString());
            for (InternalVirtualLink vlr : vnfr.getVirtual_link()) {
                logger.debug("VLR: " + vlr.toString());
                if (!vlr.getQos().isEmpty()) {
                    for (String qosAttr : vlr.getQos()) {
                        logger.debug("QoS Attribute: " + qosAttr);
                        if (qosAttr.contains("minimum_bandwith")) {
                            logger.debug("FOUND QOS ATTR WITH QOS: " + qosAttr);
                            creator.addQos(nsr.getVnfr(), nsr.getId());
                            break vnfrloop;
                        }
                    }
                }
            }
        }
    }

    public void deleteNsr(String message){

        logger.info("received new event " + message);
        OpenbatonEvent evt;

        try {
            logger.debug("Trying to deserialize it");
            evt = mapper.fromJson(message, OpenbatonEvent.class);
        } catch (JsonParseException e) {
            if (logger.isDebugEnabled() || logger.isTraceEnabled())
                logger.warn("Error in payload, expected NSR ", e);
            else
                logger.warn("Error in payload, expected NSR " + e.getMessage());
            return;
        }

        logger.debug("Deserialized!!!");
        logger.debug("ACTION: " + evt.getAction() + " PAYLOAD " + evt.getPayload().toString());
        NetworkServiceRecord nsr = evt.getPayload();

        vnfrloop:
        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {
            logger.debug("VNFR: " + vnfr.toString());
            for (InternalVirtualLink vlr : vnfr.getVirtual_link()) {
                logger.debug("VLR: " + vlr.toString());
                if (!vlr.getQos().isEmpty()) {
                    for (String qosAttr : vlr.getQos()) {
                        if (qosAttr.contains("minimum_bandwith")) {
                            logger.debug("FOUND QOS ATTR WITH QOS: " + qosAttr);
                            creator.removeQos(nsr.getVnfr(), nsr.getId());
                            break vnfrloop;
                        }
                    }
                }
            }
        }
    }

    @PreDestroy
    private void dispose() throws SDKException {
        for (String id : this.eventIds) {
            requestor.getEventAgent().delete(id);
        }
    }
}
