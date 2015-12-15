package org.nubomedia.qosmanager.beans.connectivitymanager;

import org.nubomedia.qosmanager.connectivitymanageragent.beans.ConnectivityManagerRequestor;
import org.nubomedia.qosmanager.connectivitymanageragent.json.*;
import org.nubomedia.qosmanager.openbaton.QoSAllocation;
import org.nubomedia.qosmanager.openbaton.QoSReference;
import org.nubomedia.qosmanager.values.Quality;
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
public class QoSHandler {

    @Autowired private ConnectivityManagerRequestor requestor;
    private Logger logger;

    @PostConstruct
    private void init(){
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public List<Server> createQueues(Host hostMap, List<QoSAllocation> queues){

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
        String id = "" + qos.getActualID()+1;
        QosQueue queue = new QosQueue(new QosQueueValues(quality),"",id);
        qos.addQueue(queue);
        serverIface.setQos(qos);
        return serverIface;
    }

    public void removeQos(Host hostMap, List<Server> servers, List<String> serverIds){

        for (Server server :servers){
            if (serverIds.contains(server.getName())){
                String hypervisor = hostMap.belongsTo(server.getName());
                for (InterfaceQoS iface : server.getInterfaces()){
                    Qos ifaceQoS = iface.getQos();
                    if (!ifaceQoS.getQueues().isEmpty()){
                        for(QosQueue queue : ifaceQoS.getQueues()){
                            requestor.delQueue(hypervisor,ifaceQoS.getQos_uuid(),queue.getUuid(),queue.getId());
                        }
                    }
                    requestor.delQos(hypervisor,ifaceQoS.getQos_uuid());
                }
            }
        }

    }

}
