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

/**
 * Created by maa on 02.12.15.
 */
@Service
public class QoSCreator {

    @Autowired private ConnectivityManagerHandler handler;
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void addQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){
        logger.debug("Received vnfrs with qos");

        FlowAllocation flows = this.getSFlows(vnfrs);
        logger.debug("adding flows for " + flows.toString()); //print list@id
        List<QoSAllocation> qoses = this.getQoses(vnfrs);
        logger.debug("adding qoses for " + qoses.toString());

        boolean response = handler.addQoS(qoses,flows,nsrId);
        logger.debug("RESPONSE from Handler " + response);
    }

    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){

        List<String> servers = this.getServersWithQoS(vnfrs);
        logger.debug("remmoving qos for nsr " + nsrId + " with vnfrs: " + vnfrs);
        boolean response = handler.removeQoS(servers,nsrId);
        logger.debug("Response from handler " + response);

    }

    private  List<String> getServersWithQoS(Set<VirtualNetworkFunctionRecord> vnfrs){
        List<String> res = new ArrayList<>();

        Map<String, Quality> qualities = this.getVlrs(vnfrs);

        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for (VNFCInstance vnfcInstance : vdu.getVnfc_instance()){
                    for (VNFDConnectionPoint connectionPoint : vnfcInstance.getConnection_point()){
                        if (qualities.keySet().contains(connectionPoint.getVirtual_link_reference())){
                            logger.debug("GETSERVERWITHQOS");
                            res.add(vnfcInstance.getHostname());
                        }
                    }
                }
            }
        }

        return res;
    }

    private List<QoSAllocation> getQoses(Set<VirtualNetworkFunctionRecord> vnfrs) {

        Map<String, Quality> qualities = this.getVlrs(vnfrs);
        List<QoSAllocation> res = new ArrayList<>();

        for(VirtualNetworkFunctionRecord vnfr : vnfrs){
            for(VirtualDeploymentUnit vdu : vnfr.getVdu()){
                for(VNFCInstance vnfci : vdu.getVnfc_instance()){
                    if(this.hasQoS(qualities,vnfci.getConnection_point())){
                        QoSAllocation tmp = new QoSAllocation();
                        tmp.setServerName(vnfci.getHostname());
                        List<QoSReference> ifaces = new ArrayList<>();
                        for(VNFDConnectionPoint cp : vnfci.getConnection_point()){
                            if(qualities.keySet().contains(cp.getVirtual_link_reference())){
                                QoSReference ref = new QoSReference();
                                ref.setQuality(qualities.get(cp.getVirtual_link_reference()));
                                for(Ip ip : vnfci.getIps()){
                                    if(ip.getNetName().equals(cp.getVirtual_link_reference())){
                                        ref.setIp(ip.getIp());
                                    }
                                }
                                logger.debug("GET QOSES: adding reference to list " + ref.toString());
                                ifaces.add(ref);
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

    private boolean hasQoS(Map<String,Quality> qualities, Set<VNFDConnectionPoint> ifaces){

        for(VNFDConnectionPoint cp : ifaces){
            if(qualities.keySet().contains(cp.getVirtual_link_reference()))
                return true;
        }
        return false;
    }

    private FlowAllocation getSFlows(Set<VirtualNetworkFunctionRecord> vnfrs){

        Map<String, Quality> qualities = this.getVlrs(vnfrs);
        FlowAllocation res = new FlowAllocation();

        for(String vlr : qualities.keySet()){
            List<FlowReference> references = this.findCprFromVirtualLink(vnfrs,vlr);
            res.addVirtualLink(vlr,references);
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

    private Map<String,Quality> getVlrs(Set<VirtualNetworkFunctionRecord> vnfrs) {
        Map<String,Quality> res = new LinkedHashMap<>();
        logger.debug("GETTING VLRS");
        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (InternalVirtualLink vlr : vnfr.getVirtual_link()){
                for(String qosParam: vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")){
                        Quality quality = this.mapValueQuality(qosParam);
                        res.put(vlr.getName(),quality);
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
