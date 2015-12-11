package org.nubomedia.qosmanager.beans.connectivitymanager;

import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by maa on 03.12.15.
 */
@Service
public class ConnectivityManagerHandler {

    @Autowired private QoSHandler queueHandler;
    @Autowired private FlowHandler flowsHandler;
    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;
    private Map<String, List<Server>> internalData;
    private Host hostMap;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.internalData = new LinkedHashMap<>();
        this.hostMap = new Host();
    }


    public boolean addQoS(List<QoSAllocation> queues, FlowAllocation flows, String nsrId){
        logger.debug("Start creating QOS for " + nsrId + " with queues " + queues.toString() + " and flows " + flows.toString());
        this.updateHost();
        List<Server> servers = queueHandler.createQueues(hostMap, queues);
        internalData.put(nsrId,servers);

        flowsHandler.createFlows(hostMap,servers,flows);

        return true;
    }

    private void updateHost() {
        this.hostMap = requestor.getHost();
    }

    public boolean removeQoS(){
        return true;
    }


}
