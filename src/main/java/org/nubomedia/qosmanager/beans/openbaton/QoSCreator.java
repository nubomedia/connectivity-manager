package org.nubomedia.qosmanager.beans.openbaton;

import org.nubomedia.qosmanager.beans.connectivitymanager.ConnectivityManagerHandler;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.values.Quality;
import org.openbaton.catalogue.mano.common.ConnectionPoint;
import org.openbaton.catalogue.mano.common.Ip;
import org.openbaton.catalogue.mano.descriptor.VNFDConnectionPoint;
import org.openbaton.catalogue.mano.descriptor.VirtualDeploymentUnit;
import org.openbaton.catalogue.mano.record.VNFCInstance;
import org.openbaton.catalogue.mano.record.VirtualLinkRecord;
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




    }

    public void removeQos(Set<VirtualNetworkFunctionRecord> vnfrs,String nsrId){

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
                                ifaces.add(ref);
                            }
                        }
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
                                    res.add(new FlowReference(vnfc.getHostname(),ip.getIp()));
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
        for (VirtualNetworkFunctionRecord vnfr : vnfrs){
            for (VirtualLinkRecord vlr : vnfr.getConnected_external_virtual_link()){
                for(String qosParam: vlr.getQos()) {
                    if (qosParam.contains("minimum_bandwith")){
                        Quality quality = this.mapValueQuality(qosParam);
                        res.put(vlr.getName(),quality);
                    }
                }
            }
        }
        return res;
    }

    private Quality mapValueQuality(String value){

        String[] qos = value.split(":");
        return Quality.valueOf(qos[1]);
    }



}
