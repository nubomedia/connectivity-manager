package org.nubomedia.qosmanager.connectivitymanageragent.json;

/**
 * Created by maa on 02.12.15.
 */
public class Flow {

    private String ovs_port;
    private String src_ipv4;
    private String dest_ipv4;
    private String protocol; //could be tcp/udp
    private String priority;
    private String queue_number;

    public Flow(String ovs_port, String src_ipv4, String dest_ipv4, String protocol, String priority, String queue_number) {
        this.ovs_port = ovs_port;
        this.src_ipv4 = src_ipv4;
        this.dest_ipv4 = dest_ipv4;
        this.protocol = protocol;
        this.priority = priority;
        this.queue_number = queue_number;
    }

    public Flow() {
    }

    public String getOvs_port() {
        return ovs_port;
    }

    public void setOvs_port(String ovs_port) {
        this.ovs_port = ovs_port;
    }

    public String getSrc_ipv4() {
        return src_ipv4;
    }

    public void setSrc_ipv4(String src_ipv4) {
        this.src_ipv4 = src_ipv4;
    }

    public String getDest_ipv4() {
        return dest_ipv4;
    }

    public void setDest_ipv4(String dest_ipv4) {
        this.dest_ipv4 = dest_ipv4;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getQueue_number() {
        return queue_number;
    }

    public void setQueue_number(String queue_number) {
        this.queue_number = queue_number;
    }

    @Override
    public String toString() {
        return "Flow{" +
                "ovs_port='" + ovs_port + '\'' +
                ", src_ipv4='" + src_ipv4 + '\'' +
                ", dest_ipv4='" + dest_ipv4 + '\'' +
                ", protocol='" + protocol + '\'' +
                ", priority='" + priority + '\'' +
                ", queue_number='" + queue_number + '\'' +
                '}';
    }
}
