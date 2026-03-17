package com.project.cybershield.flow;

import com.project.cybershield.decode.DecodedPacket;

/*
    Glue metoda pomocna klasa koja spajva dva dijela sustava
 */
public final class FlowKeysGen {

    private FlowKeysGen() {}

    public static FlowKey from(DecodedPacket p) {
        return new FlowKey(
                p.srcIp(),
                p.srcPort(),
                p.dstIp(),
                p.dstPort(),
                p.l4Proto()
        );
    }
}
