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
import org.nubomedia.qosmanager.connectivitymanageragent.json.Host;
import org.nubomedia.qosmanager.connectivitymanageragent.json.InterfaceQoS;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Qos;
import org.nubomedia.qosmanager.connectivitymanageragent.json.QosAdd;
import org.nubomedia.qosmanager.connectivitymanageragent.json.QosQueue;
import org.nubomedia.qosmanager.connectivitymanageragent.json.QosQueueValues;
import org.nubomedia.qosmanager.connectivitymanageragent.json.Server;
import org.nubomedia.qosmanager.connectivitymanageragent.json.ServerQoS;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.values.Quality;
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
public class QoSHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public List<Server> createQueues(Host hostMap, List<QoSAllocation> queues, String nsrId){

        logger.info("[QOS-HANDLER] CREATING queues for " + nsrId + " at time " + new Date().getTime());
        logger.debug("received request for " + queues.toString());

        List<ServerQoS> queuesReq = new ArrayList<>();
        List<Server> servers = new ArrayList<>();

        for(QoSAllocation allocation : queues){

            String serverName = allocation.getServerName();
            logger.debug("[CREATING QUEUES] get server name " + serverName);
            String hypervisor = hostMap.belongsTo(serverName);
            logger.debug("[CREATING QUEUES] get hypervisor name " + hypervisor);
            Server serverData = requestor.getServerData(hypervisor,serverName);
            logger.debug("[CREATING QUEUES] server data is " + serverData.toString() );
            servers.add(serverData);
            ServerQoS serverQoS = this.compileServerRequest(serverData,allocation.getIfaces(),hypervisor);
            queuesReq.add(serverQoS);
        }

        QosAdd add = new QosAdd(queuesReq);
        add = requestor.setQoS(add);

        servers = this.updateServers(servers, add);
        logger.info("[QOS-HANDLER] CREATED queues for " + nsrId + " at time " + new Date().getTime());
        return servers;
    }

    private List<Server> updateServers(List<Server> servers, QosAdd add) {

        List<Server> updated = new ArrayList<>();
        for(Server server : servers){
            ServerQoS qos = add.getQosByServerID(server.getId());
            if (qos != null){
                server.updateInterfaces(qos.getInterfaces());
            }
            updated.add(server);
        }

        return updated;
    }

    private ServerQoS compileServerRequest(Server serverData, List<QoSReference> ifaces, String hypervisor) {

        logger.debug("[COMPILE SERVER REQUEST] Server data: " + serverData.toString() + " hypervisor " + hypervisor);
        ServerQoS res = new ServerQoS();
        res.setHypervisorId(hypervisor);
        res.setServerId(serverData.getId());

        List<InterfaceQoS> ifacesReq = new ArrayList<>();
        for(InterfaceQoS serverIface : serverData.getInterfaces()){
            for(QoSReference ref : ifaces){
                if(serverIface.getIp().equals(ref.getIp())){
                    InterfaceQoS iface = this.addQuality(serverIface,ref.getQuality());
                    ifacesReq.add(iface);
                }
            }
        }
        res.setInterfaces(ifacesReq);

        return res;
    }

    private InterfaceQoS addQuality(InterfaceQoS serverIface, Quality quality) {

        Qos qos = serverIface.getQos();
        int idNum = qos.getActualID()+1;
        String id = "" + idNum;
        QosQueue queue = new QosQueue(new QosQueueValues(quality),"",id);
        qos.addQueue(queue);
        serverIface.setQos(qos);
        return serverIface;
    }

    public void removeQos(Host hostMap, List<Server> servers, List<String> serverIds, String nsrId){

        logger.info("[QOS-HANDLER] REMOVING queues for " + nsrId + " at time " + new Date().getTime());
        for (Server server :servers){
            boolean contains = false;
            for (String id : serverIds) {
                if (server.getName().contains(id)) {
                    contains = true;
                }
            }
            if (contains){
                String hypervisor = hostMap.belongsTo(server.getName());
                for (InterfaceQoS iface : server.getInterfaces()){
                    Qos ifaceQoS = iface.getQos();
                    requestor.delQos(hypervisor,ifaceQoS.getQos_uuid());
                }
            }
        }
        logger.info("[QOS-HANDLER] REMOVED queues for " + nsrId + " at time " + new Date().getTime());

    }

}
