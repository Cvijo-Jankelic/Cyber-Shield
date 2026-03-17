package com.project.cybershield.detect;


import com.project.cybershield.host.HostState;

//puno SYN paketa (pokušaji otvaranja veze)
    //
    //malo ili nimalo ACK (veze se ne dovrše)
    //→ sumnja na SYN flood ili masovno “half-open” ponašanje.
public class SynFloodDetector {
    private final long winodwMillis;
    private final long synThreshold;
    private final long ackMax;
    private final double synAckRatioThreshold;
    private final long cooldownMillis;

    public SynFloodDetector(long windowMillis, long synThreshold, long ackMax, double synAckRatioThreshold, long cooldownMillis) {
        this.winodwMillis = windowMillis;
        this.synThreshold = synThreshold;
        this.ackMax = ackMax;
        this.synAckRatioThreshold = synAckRatioThreshold;
        this.cooldownMillis = cooldownMillis;
    }

    public long windowMillis() {
        return  winodwMillis;
    }

    public boolean isSynFloodLike(HostState hs){
        long syn = hs.synPacketsInWindow();
        long ack = hs.ackPacketsInWindow();

        if(syn < synThreshold) return false;

        if(ack <= ackMax) return true;
        // Omjer SYN/ACK paketa formula za prevelik omjer
        double ratio = (ack == 0) ? Double.POSITIVE_INFINITY : (double)syn / (double)ack;
        return ratio >= synAckRatioThreshold;
    }
    public boolean canAlertNow(HostState hs, long nowMillis){
        long last = hs.lastAlertMillis();
        return last < 0 || (nowMillis - last) >= cooldownMillis;
    }
}
