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

import org.nubomedia.qosmanager.interfaces.QoSInterface;
import org.nubomedia.qosmanager.openbaton.VldQuality;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFComponent;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by maa on 18.01.16.
 */
public class RemoveQoSExecutor implements Runnable{

    private QoSInterface connectivityManagerHandler;
    private Logger logger;
    private Set<VirtualNetworkFunctionRecord> vnfrs;
    private String nsrID;

    public RemoveQoSExecutor(QoSInterface connectivityManagerHandler, Set<VirtualNetworkFunctionRecord> vnfrs, String nsrID) {
        this.connectivityManagerHandler = connectivityManagerHandler;
        this.vnfrs = vnfrs;
        this.nsrID = nsrID;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    @Override
    public void run() {
        logger.info("[REMOVE-QOS-EXECUTOR] deleting slice for " + nsrID + " at time " + new Date().getTime());
        List<String> servers = this.getServersWithQoS(vnfrs);
        logger.debug("remmoving qos for nsr " + nsrID + " with vnfrs: " + vnfrs);
        boolean response = connectivityManagerHandler.removeQoS(servers,nsrID);
        logger.debug("Response from handler " + response);
        logger.info("[REMOVE-QOS-EXECUTOR] ended slice removal for " + nsrID + " at time " + new Date().getTime());
    }

    private  List<String> getServersWithQoS(Set<VirtualNetworkFunctionRecord> vnfrs){
        List<String> res = new ArrayList<>();

        List<VldQuality> qualities = this.getVlrs(vnfrs);

        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for (VNFComponent vnfc : vdu.getVnfc()) {
                    for (VNFDConnectionPoint connectionPoint : vnfc.getConnection_point()){
                        for (VldQuality quality : qualities) {
                            if (quality.getVnfrId().equals(vnfr.getId()) && quality.getVlid().equals(connectionPoint.getVirtual_link_reference())) {
                                logger.debug("GETSERVERWITHQOS");
                                res.add(vdu.getHostname());
                            }
                        }
                    }
                }
            }
        }

        return res;
    }

    private List<VldQuality> getVlrs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        List<VldQuality> res = new ArrayList<>();
        logger.debug("GETTING VLRS");
        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (InternalVirtualLink vlr : vnfr.getVirtual_link()){
                for(String qosParam: vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")){
                        Quality quality = this.mapValueQuality(qosParam);
                        VldQuality vldQuality = new VldQuality(vnfr.getId(),vlr.getName(),quality);
                        res.add(vldQuality);
                        logger.debug("GET VIRTUAL LINK RECORD: insert in map vlr name " + vlr.getName() + " with quality " + quality);
                    }
                }
            }
        }
        return res;
    }

    private Quality mapValueQuality(String value){
        logger.debug("MAPPING VALUE-QUALITY: received value " + value);
        String[] qos = value.split(":");
        logger.debug("MAPPING VALUE-QUALITY: quality is " + qos[1]);
        return Quality.valueOf(qos[1]);
    }
}
