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

package org.nubomedia.qosmanager.beans.connectivitymanager;

import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Flow;
import org.nubomedia.qosmanager.connectivitymanageragent.json.FlowServer;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.InterfaceQoS;
import org.nubomedia.qosmanager.connectivitymanageragent.json.RequestFlows;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.openbaton.FlowAllocation;
import org.nubomedia.qosmanager.openbaton.FlowReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

/**
 * Created by maa on 09.12.15.
 */
@Service
@Scope ("prototype")
public class FlowHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;
    private String protocol;
    private String priority;

    @PostConstruct
    private void init(){

        this.logger = LoggerFactory.getLogger(this.getClass());
        this.protocol = "udp";
        this.priority = "2";

    }

    public void createFlows(Host host, List<Server> servers, FlowAllocation allocations, String nsrId){
        logger.info("[FLOW-HANDLER] CREATING flows for " + nsrId + " at time " + new Date().getTime());
        logger.debug("Received Flow allocation " + allocations.toString());
        List<FlowServer> flows = new ArrayList<>();
        for (String vlr : allocations.getAllVlr()){
            for (FlowReference fr : allocations.getIpsForVlr(vlr)){
                for(Server server : servers){
                    if(server.getName().equals(fr.getHostname())){
                        FlowServer fs = new FlowServer();
                        fs.setHypervisor_id(host.belongsTo(server.getName()));
                        fs.setServer_id(server.getName());
                        InterfaceQoS iface = server.getFromIp(fr.getIp());
                        List<Flow> internalFlows = new ArrayList<>();
                        for(String ip : allocations.getAllIpsForVlr(vlr)){
                            if (!ip.equals(fr.getIp())) {
                                Flow tmp = new Flow();
                                tmp.setDest_ipv4(ip);
                                Server dest = this.getServerRefFromIp(servers,ip);
                                tmp.setDest_hyp(host.belongsTo(dest.getName()));
                                tmp.setOvs_port_number(dest.getFromIp(ip).getOvs_port_number());
                                tmp.setPriority(priority);
                                tmp.setProtocol(protocol);
                                tmp.setSrc_ipv4(iface.getIp());
                                tmp.setQueue_number("" + dest.getFromIp(ip).getQos().getActualID());
                                internalFlows.add(tmp);
                            }
                        }
                        fs.setQos_flows(internalFlows);
                        flows.add(fs);
                    }
                }
            }
        }
        RequestFlows request = new RequestFlows(flows);
        logger.debug("REQUEST is " + request.toString());
        RequestFlows returningFlows = requestor.setFlow(request);
        logger.debug("Returning flows " + returningFlows.toString());
        logger.info("[FLOW-HANDLER] CREATED queues for " + nsrId + " at time " + new Date().getTime());
    }

    public void removeFlows(Host hostmap, List<String> serversIds, List<Server> servers, String nsrId){

        logger.info("[FLOW-HANDLER] REMOVING queues for " + nsrId + " at time " + new Date().getTime());

        for(Server server : servers){
            boolean contains = false;
            for (String id : serversIds) {
                if (server.getName().contains(id)) {
                    contains = true;
                }
            }
            if (contains){
                String hypervisor = hostmap.belongsTo(server.getName());
                for  (InterfaceQoS iface : server.getInterfaces()){
                    requestor.deleteFlow(hypervisor,protocol,iface.getIp());
                }
            }
        }
        logger.info("[FLOW-HANDLER] REMOVED queues for " + nsrId + " at time " + new Date().getTime());
    }

    private Server getServerRefFromIp (List<Server> servers, String ip){


        for (Server server : servers){
            if(server.getFromIp(ip) != null){
                return server;
            }
        }

        return null;
    }

}
