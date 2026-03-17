package com.project.cybershield.decode;

import com.project.cybershield.entities.Protocol;

public record DecodedPacket(
        long timestampMillis,
        String srcMac,
        String dstMac,
        Integer vlanId,

        String srcIp,
        String dstIp,
        Protocol l4Proto,

        Integer srcPort,
        Integer dstPort,

        byte[] payload,
        TcpFlags tcpFlags
) {
    public boolean isIp(){return srcIp != null && dstIp != null;}
    public boolean isTcp(){return l4Proto == Protocol.TCP; }
    public boolean isUdp(){return l4Proto == Protocol.UDP; }
    public boolean isIcmp(){return l4Proto == Protocol.ICMP; }
}
