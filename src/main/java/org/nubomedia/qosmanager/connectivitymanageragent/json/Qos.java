package org.nubomedia.qosmanager.connectivitymanageragent.json;

import javax.persistence.*;
import java.util.List;

/**
 * Created by maa on 04.11.15.
 */
//@Entity
public class Qos {

//    @Id
    private String qos_uuid;
//    @OneToMany(targetEntity = QosQueue.class,cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<QosQueue> queues;

    public Qos() {
    }

    public Qos(List<QosQueue> queues, String qos_uuid) {
        this.queues = queues;
        this.qos_uuid = qos_uuid;
    }

    public List<QosQueue> getQueues() {
        return queues;
    }

    public void setQueues(List<QosQueue> queues) {
        this.queues = queues;
    }

    public String getQos_uuid() {
        return qos_uuid;
    }

    public void setQos_uuid(String qos_uuid) {
        this.qos_uuid = qos_uuid;
    }

    public int getActualID (){
        return queues.size(); //DO NOT CREATE QUEUE WITH ID 0 ON OVS!!!
    }

    public void addQueue (QosQueue queue){
        this.queues.add(queue);
    }
}
