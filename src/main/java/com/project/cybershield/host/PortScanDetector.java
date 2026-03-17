package com.project.cybershield.host;

public class PortScanDetector {
    private final long windowMillis;
    private final int uniquePortThreshold;
    private final long synThreshold;

    public PortScanDetector(long windowMillis, int uniquePortThreshold, long synThreshold) {
        this.windowMillis = windowMillis;
        this.uniquePortThreshold = uniquePortThreshold;
        this.synThreshold = synThreshold;
    }

    public boolean isPortScan(HostState hostState){
        return hostState.uniqueDstPortsInWindow() >= uniquePortThreshold
                && hostState.synPacketsInWindow() >= synThreshold;
    }

    public long windowMillis(){
        return windowMillis;
    }
}
