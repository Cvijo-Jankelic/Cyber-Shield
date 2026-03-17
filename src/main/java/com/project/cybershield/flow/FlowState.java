package com.project.cybershield.flow;

import com.project.cybershield.decode.DecodedPacket;

import java.util.HashSet;
import java.util.Set;

public class FlowState {
    private final FlowKey flowKey;
    private final long firstSeenMillis;
    private long lastSeenMillis;
    private long packetCount;
    private long byteCount;

    private long synCount;
    private long synAckCount;
    private long ackCount;
    private long rstCount;
    private long finCount;

    // korisno za scan flood detekcije
    private final Set<Integer> uniqueDstPorts = new HashSet<>();

    public FlowState(FlowKey flowKey, long nowMillis) {
        this.flowKey = flowKey;
        this.firstSeenMillis = nowMillis;
        this.lastSeenMillis = nowMillis;
        this.packetCount = 0;
        this.byteCount = 0;
    }

    public void update(long nowMillis, int payLoad, DecodedPacket p){
        lastSeenMillis = nowMillis;
        packetCount++;
        byteCount += payLoad;

        if(flowKey.dstPort() != null){
            uniqueDstPorts.add(flowKey.dstPort());
        }
        /* */
        if(p.isTcp()){
            var f = p.tcpFlags();
            if (f.syn() && !f.ack()) synCount++;
            if (f.syn() && f.ack()) synAckCount++;
            if (f.ack()) ackCount++;
            if (f.rst()) rstCount++;
            if (f.fin()) finCount++;
        }

    }

    public FlowKey getFlowKey() {
        return flowKey;
    }

    public long getFirstSeenMillis() {
        return firstSeenMillis;
    }

    public long getLastSeenMillis() {
        return lastSeenMillis;
    }

    public long getPacketCount() {
        return packetCount;
    }

    public long getByteCount() {
        return byteCount;
    }

    public Set<Integer> getUniqueDstPorts() {
        return Set.copyOf(uniqueDstPorts);
    }
}
