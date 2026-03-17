package com.project.cybershield.network;

import org.pcap4j.packet.Packet;

public interface PacketConsumer extends AutoCloseable{
    void accept(Packet packet);
}
