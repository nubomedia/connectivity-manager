package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 10/11/2015.
 */
public class Host {

    private List<Datacenter> hypervisors;

    public Host(List<Datacenter> hypervisors) {
        this.hypervisors = hypervisors;
    }

    public Host() {
    }

    public List<Datacenter> getHosts() {
        return hypervisors;
    }

    public void setHosts(List<Datacenter> hypervisors) {
        this.hypervisors = hypervisors;
    }

    public String belongsTo(String serverName){

        for (Datacenter datacenter : hypervisors){

            if (datacenter.getServers().contains(serverName)){

                return datacenter.getName();

            }

        }

        return null;
    }
}
