package com.project.cybershield.enums;

public enum SpeedLimit {
    SLOW(1 * 512),
    MEDIUM(500 * 1024),
    FAST(1024 * 1024),
    UNLIMITED(Long.MAX_VALUE);

    public final long bytesPerSecond;
    SpeedLimit(long bps){
        this.bytesPerSecond = bps;
    }

}
