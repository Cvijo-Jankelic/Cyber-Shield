package com.project.cybershield.flow;

import com.project.cybershield.entities.Protocol;

public record FlowKey(
        String srcIp,
        Integer srcPort,
        String dstIp,
        Integer dstPort,
        Protocol protocol
) {
}
