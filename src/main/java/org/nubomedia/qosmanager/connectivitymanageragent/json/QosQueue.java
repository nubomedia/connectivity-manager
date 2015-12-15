package org.nubomedia.qosmanager.connectivitymanageragent.json;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by maa on 10.11.15.
 */
//@Entity
public class QosQueue {

//    @Id
    private String queue_uuid;
    private QosQueueValues rates;
    private String id;

    public QosQueue() {
    }

    public QosQueue(QosQueueValues rates, String queue_uuid, String id) {
        this.rates = rates;
        this.queue_uuid = queue_uuid;
        this.id = id;
    }

    public QosQueueValues getRates() {
        return rates;
    }

    public void setRates(QosQueueValues rates) {
        this.rates = rates;
    }

    public String getUuid() {
        return queue_uuid;
    }

    public void setUuid(String queue_uuid) {
        this.queue_uuid = queue_uuid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "QosQueue{" +
                "queue_uuid='" + queue_uuid + '\'' +
                ", rates=" + rates +
                ", id='" + id + '\'' +
                '}';
    }
}
