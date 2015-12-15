package org.nubomedia.qosmanager.openbaton;

/**
 * Created by maa on 09.12.15.
 */
public class FlowReference {

    private String hostname;
    private String ip;

    public FlowReference(String hostname, String ip) {
        this.hostname = hostname;
        this.ip = ip;
    }

    public FlowReference() {
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return "FlowReference{" +
                "hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                '}';
    }
}
