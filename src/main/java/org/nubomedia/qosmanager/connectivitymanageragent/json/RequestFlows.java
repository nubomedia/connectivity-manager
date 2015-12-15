package org.nubomedia.qosmanager.connectivitymanageragent.json;

import java.util.List;

/**
 * Created by maa on 02.12.15.
 */
public class RequestFlows {

    private List<FlowServer> flows;

    public RequestFlows(List<FlowServer> flows) {
        this.flows = flows;
    }

    public RequestFlows() {
    }

    public List<FlowServer> getFlows() {
        return flows;
    }

    public void setFlows(List<FlowServer> flows) {
        this.flows = flows;
    }

    @Override
    public String toString() {
        return "RequestFlows{" +
                "flows=" + flows +
                '}';
    }
}
