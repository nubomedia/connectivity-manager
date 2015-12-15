package org.nubomedia.qosmanager.connectivitymanageragent.json;


import com.google.gson.annotations.SerializedName;
import org.nubomedia.qosmanager.values.Quality;

/**
 * Created by maa on 04.11.15.
 */
public class QosQueueValues {

    @SerializedName("min-rate") private String min_bitrate;
    @SerializedName("max-rate") private String max_bitrate;

    public QosQueueValues() {
    }

    public QosQueueValues(String min_bitrate, String max_bitrate) {
        this.min_bitrate = min_bitrate;
        this.max_bitrate = max_bitrate;
    }

    public QosQueueValues(Quality quality){

        this.min_bitrate = quality.getMin_rate();
        this.max_bitrate = quality.getMax_rate();
    }

    public String getMin_bitrate() {
        return min_bitrate;
    }

    public void setMin_bitrate(String min_bitrate) {
        this.min_bitrate = min_bitrate;
    }

    public String getMax_bitrate() {
        return max_bitrate;
    }

    public void setMax_bitrate(String max_bitrate) {
        this.max_bitrate = max_bitrate;
    }

//    public Quality getQuality (){
//        Quality quality ;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QosQueueValues)) return false;

        QosQueueValues that = (QosQueueValues) o;

        if (!getMin_bitrate().equals(that.getMin_bitrate())) return false;
        return getMax_bitrate().equals(that.getMax_bitrate());

    }

    @Override
    public int hashCode() {
        int result = getMin_bitrate().hashCode();
        result = 31 * result + getMax_bitrate().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "QosQueueValues{" +
                "min_bitrate='" + min_bitrate + '\'' +
                ", max_bitrate='" + max_bitrate + '\'' +
                '}';
    }
}
