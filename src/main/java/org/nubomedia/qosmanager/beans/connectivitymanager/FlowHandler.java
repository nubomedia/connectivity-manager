package org.nubomedia.qosmanager.beans.connectivitymanager;

import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.*;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maa on 09.12.15.
 */
@Service
public class FlowHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;
    private String protocol;
    private String priority;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());
        this.protocol = "tcp";
        this.priority = "1";

    }

    public void createFlows(Host host, List<Server> servers, FlowAllocation allocations){
        logger.debug("Received Flow allocation " + allocations.toString());
        List<FlowServer> flows = new ArrayList<>();
        for (String vlr : allocations.getAllVlr()){
            for (FlowReference fr : allocations.getIpsForVlr(vlr)){
                for(Server server: servers){
                    if(server.getName().equals(fr.getHostname())){
                        FlowServer fs = new FlowServer();
                        fs.setHypervisor_id(host.belongsTo(server.getName()));
                        fs.setServer_id(server.getName());
                        InterfaceQoS iface = server.getFromIp(fr.getIp());
                        List<Flow> internalFlows = new ArrayList<>();
                        for(String ip : allocations.getAllIpsForVlr(vlr)){
                            Flow tmp = new Flow();
                            tmp.setDest_ipv4(ip);
                            tmp.setOvs_port(iface.getOvs_port_number());
                            tmp.setPriority(priority);
                            tmp.setProtocol(protocol);
                            tmp.setSrc_ipv4(iface.getIp());

                            internalFlows.add(tmp);
                        }
                        fs.setQos_flows(internalFlows);
                        flows.add(fs);
                    }
                }
            }
        }
        RequestFlows returningFlows = requestor.setFlow(new RequestFlows(flows));
        logger.debug("Returning flows " + returningFlows.toString());
    }

    public void removeFlows(Host hostmap, List<String> serversIds, List<Server> servers){

        for(Server server : servers){
            if (serversIds.contains(server.getName())){
                String hypervisor = hostmap.belongsTo(server.getName());
                for  (InterfaceQoS iface : server.getInterfaces()){
                    requestor.deleteFlow(hypervisor,protocol,iface.getIp());
                }
            }
        }
    }

}
