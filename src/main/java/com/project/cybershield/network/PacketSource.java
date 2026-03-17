package com.project.cybershield.network;


import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.packet.Packet;

public interface PacketSource extends AutoCloseable {
    void start() throws Exception;

    Packet nextPacket() throws Exception;

    boolean isOpen();

    void close();
}
