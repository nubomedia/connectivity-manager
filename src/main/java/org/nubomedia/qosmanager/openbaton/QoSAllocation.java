package org.nubomedia.qosmanager.openbaton;

import java.util.List;

/**
 * Created by maa on 09.12.15.
 */
public class QoSAllocation {

    private String serverName;
    private List<QoSReference> ifaces;

    public QoSAllocation(String serverName, List<QoSReference> ifaces) {
        this.serverName = serverName;
        this.ifaces = ifaces;
    }

    public QoSAllocation() {
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public List<QoSReference> getIfaces() {
        return ifaces;
    }

    public void setIfaces(List<QoSReference> ifaces) {
        this.ifaces = ifaces;
    }

    @Override
    public String toString() {
        return "QoSAllocation{" +
                "serverName='" + serverName + '\'' +
                ", ifaces=" + ifaces +
                '}';
    }
}
