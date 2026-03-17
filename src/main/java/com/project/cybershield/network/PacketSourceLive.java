package com.project.cybershield.network;

import org.pcap4j.packet.Packet;

public interface PacketSourceLive extends AutoCloseable {
    void start(PacketConsumer consumer) throws Exception;

    Packet nextPacket() throws Exception;

    boolean isOpen();

    @Override
    void close();
}
