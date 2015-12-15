package org.nubomedia.qosmanager.openbaton;

import org.nubomedia.qosmanager.values.Quality;

/**
 * Created by maa on 09.12.15.
 */
public class QoSReference {

    private String ip;
    private Quality quality;

    public QoSReference(String ip, Quality quality) {
        this.ip = ip;
        this.quality = quality;
    }

    public QoSReference() {
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Quality getQuality() {
        return quality;
    }

    public void setQuality(Quality quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return "QoSReference{" +
                "ip='" + ip + '\'' +
                ", quality=" + quality +
                '}';
    }
}
