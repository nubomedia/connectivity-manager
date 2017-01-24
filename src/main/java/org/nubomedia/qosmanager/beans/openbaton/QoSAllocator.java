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

import org.nubomedia.qosmanager.beans.connectivitymanager.ConnectivityManagerHandler;
import org.nubomedia.qosmanager.interfaces.QoSInterface;
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

    @Autowired private QoSInterface handler;
    private final ScheduledExecutorService qtScheduler = Executors.newScheduledThreadPool(1);
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void addQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.info("[QOS-ALLOCATOR] received new set of vnfrs for " + nsrId + " to create a network slice at time " + new Date().getTime());
        logger.debug("Creating ADD Thread");
        AddQoSExecutor aqe = new AddQoSExecutor(handler,vnfrs,nsrId);
        qtScheduler.schedule(aqe,100, TimeUnit.MILLISECONDS);
        logger.info("[QOS-ALLOCATOR] scheduled thread to handle the NSR" + nsrId + " to create a network slice at time " + new Date().getTime());
        logger.debug("ADD Thread created and scheduled");
    }

    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.info("[QOS-ALLOCATOR] received new set of vnfrs for " + nsrId + " to remove a network slice at time " + new Date().getTime());
        logger.debug("Creating REMOVE Thread");
        RemoveQoSExecutor rqe = new RemoveQoSExecutor(handler,vnfrs,nsrId);
        qtScheduler.schedule(rqe,10,TimeUnit.SECONDS);
        logger.info("[QOS-ALLOCATOR] scheduled thread to handle the NSR" + nsrId + " to remove a network slice at time " + new Date().getTime());
        logger.debug("REMOVE Thread created and scheduled");

    }

}
