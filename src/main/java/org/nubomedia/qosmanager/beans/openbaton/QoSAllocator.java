package org.nubomedia.qosmanager.beans.openbaton;

import org.nubomedia.qosmanager.beans.connectivitymanager.ConnectivityManagerHandler;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by maa on 02.12.15.
 */
@Service
public class QoSAllocator {

    @Autowired private ConnectivityManagerHandler handler;
    private final ScheduledExecutorService qtScheduler = Executors.newScheduledThreadPool(1);
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void addQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.debug("Creating ADD Thread");
        AddQoSExecutor aqe = new AddQoSExecutor(handler,vnfrs,nsrId);
        qtScheduler.schedule(aqe,100, TimeUnit.MILLISECONDS);
        logger.debug("ADD Thread created and scheduled");
    }

    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.debug("Creating REMOVE Thread");
        RemoveQoSExecutor rqe = new RemoveQoSExecutor(handler,vnfrs,nsrId);
        qtScheduler.schedule(rqe,10,TimeUnit.SECONDS);
        logger.debug("REMOVE Thread created and scheduled");

    }

}
