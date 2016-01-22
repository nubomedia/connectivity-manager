package org.nubomedia.qosmanager.values;

/**
 * Created by maa on 11.11.15.
 */
public enum Quality {

    GOLD ("1000000000","500000000"),
    SILVER ("500000000","250000000"),
    BRONZE ("250000000","125000000");

    private String max_rate;
    private String min_rate;

    Quality(String max_rate, String min_rate) {
        this.max_rate = max_rate;
        this.min_rate = min_rate;
    }

    public String getMax_rate() {
        return max_rate;
    }

    public void setMax_rate(String max_rate) {
        this.max_rate = max_rate;
    }

    public String getMin_rate() {
        return min_rate;
    }

    public void setMin_rate(String min_rate) {
        this.min_rate = min_rate;
    }
}
