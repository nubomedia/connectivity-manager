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
import org.nubomedia.qosmanager.connectivitymanageragent.json.Qos;
import org.nubomedia.qosmanager.interfaces.QoSInterface;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.openbaton.VldQuality;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.InternalVirtualLink;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualNetworkFunctionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by maa on 18.01.16.
 */
public class AddQoSExecutor implements Runnable{

    private QoSInterface connectivityManagerHandler;
    private Logger logger;
    private Set<VirtualNetworkFunctionRecord> vnfrs;
    private String nsrID;

    public AddQoSExecutor(QoSInterface connectivityManagerHandler, Set<VirtualNetworkFunctionRecord> vnfrs, String nsrID) {
        this.connectivityManagerHandler = connectivityManagerHandler;
        this.vnfrs = vnfrs;
        this.nsrID = nsrID;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void run() {

        logger.info("[ADD-QOS-EXECUTOR] allocating slice for " + nsrID + " at time " + new Date().getTime());
        logger.debug("Received vnfrs with qos");

        FlowAllocation flows = this.getSFlows(vnfrs);
        logger.debug("adding flows for " + flows.toString()); //print list@id
        List<QoSAllocation> qoses = this.getQoses(vnfrs);
        logger.debug("adding qoses for " + qoses.toString());

        boolean response = connectivityManagerHandler.addQoS(qoses,flows,nsrID);
        logger.debug("RESPONSE from Handler " + response);
        logger.info("[ADD-QOS-EXECUTOR] ended slice allocation for " + nsrID + " at time " + new Date().getTime());

    }

    private List<QoSAllocation> getQoses(Set<VirtualNetworkFunctionRecord> vnfrs) {

        List<VldQuality> qualities = this.getVlrs(vnfrs);
        List<QoSAllocation> res = new ArrayList<>();

        for(VirtualNetworkFunctionRecord vnfr : vnfrs){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for(VNFCInstance vnfci : vdu.getVnfc_instance()){
                    if(this.hasQoS(qualities,vnfci.getConnection_point(),vnfr.getId())){
                        QoSAllocation tmp = new QoSAllocation();
                        tmp.setServerName(vnfci.getHostname());
                        List<QoSReference> ifaces = new ArrayList<>();
                        for(VNFDConnectionPoint cp : vnfci.getConnection_point()){
                            for (VldQuality quality : qualities) {
                                if (quality.getVlid().equals(cp.getVirtual_link_reference()) && quality.getVnfrId().equals(vnfr.getId())) {
                                    QoSReference ref = new QoSReference();
                                    ref.setQuality(quality.getQuality());
                                    for (Ip ip : vnfci.getIps()) {
                                        if (ip.getNetName().equals(cp.getVirtual_link_reference())) {
                                            ref.setIp(ip.getIp());
                                        }
                                    }
                                    logger.debug("GET QOSES: adding reference to list " + ref.toString());
                                    ifaces.add(ref);
                                }
                            }
                        }
                        logger.debug("IFACES " + ifaces);
                        tmp.setIfaces(ifaces);
                        logger.debug("QoSAllocation " + tmp);
                        res.add(tmp);
                    }
                }
            }
        }

        return res;
    }

    private boolean hasQoS(List<VldQuality> qualities, Set<VNFDConnectionPoint> ifaces, String vnfrId){

        for(VNFDConnectionPoint cp : ifaces){
            for (VldQuality vldQuality : qualities) {
                if(vnfrId.equals(vldQuality.getVnfrId()) && cp.getVirtual_link_reference().equals(vldQuality.getVlid()))
                    return true;
            }
        }
        return false;
    }

    private FlowAllocation getSFlows(Set<VirtualNetworkFunctionRecord> vnfrs){

        List<VldQuality> qualities = this.getVlrs(vnfrs);
        FlowAllocation res = new FlowAllocation();

        for(VldQuality quality : qualities){
            List<FlowReference> references = this.findCprFromVirtualLink(vnfrs,quality.getVlid());
            res.addVirtualLink(quality.getVlid(),references);
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
                        logger.info("VLDQUALITY: VNFR ID " + vnfr.getId() + " VLR " + vlr.getName() + " QUALITY " + quality);
                        VldQuality vldQuality = new VldQuality(vnfr.getId(),vlr.getName(),quality);

                        res.add(vldQuality);
                        logger.debug("GET VIRTUAL LINK RECORD: insert in map vlr name " + vlr.getName() + " with quality " + quality);
                    }
                }
            }
        }
        return res;
    }

    private List<FlowReference> findCprFromVirtualLink(Set<VirtualNetworkFunctionRecord> vnfrs, String vlr){

        List<FlowReference> res = new ArrayList<>();

        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for(VNFCInstance vnfc : vdu.getVnfc_instance()){
                    for(VNFDConnectionPoint cp : vnfc.getConnection_point()){
                        if(cp.getVirtual_link_reference().equals(vlr)){
                            for(Ip ip : vnfc.getIps()){
                                if(ip.getNetName().equals(cp.getVirtual_link_reference())){
                                    FlowReference tobeAdded = new FlowReference(vnfc.getHostname(),ip.getIp());
                                    logger.debug("FIND CONNECTION POINT RECORD FROM VIRTUAL LINK: adding Flow Reference to list " + tobeAdded.toString());
                                    res.add(tobeAdded);
                                }
                            }
                        }
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
