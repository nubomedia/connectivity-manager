package org.nubomedia.qosmanager.beans.openbaton;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.nubomedia.qosmanager.openbaton.OpenbatonEvent;
import org.nubomedia.qosmanager.utils.ConfigReader;
import org.nubomedia.qosmanager.utils.ConfigurationBeans;
import org.openbaton.catalogue.mano.record.NetworkServiceRecord;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
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
import java.io.IOException;
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
    QoSCreator creator;
    @Autowired
    private Gson mapper;


    @PostConstruct
    private void init() throws SDKException, IOException {

        this.properties = ConfigReader.readProperties();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.requestor = new NFVORequestor(properties.getProperty("nfvo.username"), properties.getProperty("nfvo.password"), properties.getProperty("nfvo.baseURL"), properties.getProperty("nfvo.basePort"), "1");

        EventEndpoint eventEndpointCreation = new EventEndpoint();
        eventEndpointCreation.setType(EndpointType.RABBIT);
        eventEndpointCreation.setEvent(Action.INSTANTIATE_FINISH);
        eventEndpointCreation.setEndpoint(ConfigurationBeans.queueName_eventInstatiateFinish);
        eventEndpointCreation.setName("eventNSRCreated");
        requestor.getEventAgent().create(eventEndpointCreation);

        EventEndpoint eventEndpointDeletion = new EventEndpoint();
        eventEndpointDeletion.setType(EndpointType.RABBIT);
        eventEndpointDeletion.setEvent(Action.RELEASE_RESOURCES_FINISH);
        eventEndpointDeletion.setEndpoint(ConfigurationBeans.queueName_eventResourcesReleaseFinish);
        eventEndpointDeletion.setName("eventNSRCreated");
        requestor.getEventAgent().create(eventEndpointDeletion);

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


        NetworkServiceRecord nsr = evt.getPayload();

        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {

            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()) {
                if (!vlr.getQos().isEmpty()) {
                    for (String qosAttr : vlr.getQos()) {
                        if (qosAttr.contains("minimum_bandwith")) {
                            creator.addQos(nsr.getVnfr(), nsr.getId());
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


        NetworkServiceRecord nsr = evt.getPayload();

        for (VirtualNetworkFunctionRecord vnfr : nsr.getVnfr()) {

            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()) {
                if (!vlr.getQos().isEmpty()) {
                    for (String qosAttr : vlr.getQos()) {
                        if (qosAttr.contains("minimum_bandwith")) {
                            creator.removeQos(nsr.getVnfr(), nsr.getId());
                        }
                    }
                }
            }
        }
    }
}
