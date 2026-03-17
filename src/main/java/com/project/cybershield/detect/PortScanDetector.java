package com.project.cybershield.detect;

import com.project.cybershield.host.HostState;

public class PortScanDetector {
    private final long windowMillis;                // npr. 10s
    private final int uniquePortThreshold;          // npr 20 portova
    private final long synThreshold;                // npr 30 syn paketa
    private final long cooldownMillis;              // npr 10s da ne spama


    public PortScanDetector(long windowMillis, int uniquePortThreshold, long synThreshold, long cooldownMillis) {
        this.windowMillis = windowMillis;
        this.uniquePortThreshold = uniquePortThreshold;
        this.synThreshold = synThreshold;
        this.cooldownMillis = cooldownMillis;
    }

    // Koliko traje window u kojem brojimo portove/SYN paketa
    public long windowMillis() {return windowMillis;}

    /** Je li host presao pragove za scan unutar trenutnog windowa. */
    public boolean isPortScan(HostState hostState) {
        return hostState.uniqueDstPortsInWindow() >= uniquePortThreshold
                && hostState.synPacketsInWindow() >= synThreshold;
    }

    public boolean canAlertNow(HostState hs, long nowMillis){
        long last = hs.lastAlertMillis();
        return last < 0 || (nowMillis - last) >= cooldownMillis;
    }

}
