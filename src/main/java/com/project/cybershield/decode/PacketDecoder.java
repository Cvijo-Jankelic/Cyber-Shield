package com.project.cybershield.decode;

import com.project.cybershield.entities.Protocol;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.packet.*;

public class PacketDecoder {

    public static DecodedPacket decode(Packet packet, PcapHandle handleOrNull){
        long ts = (handleOrNull != null && handleOrNull.getTimestamp() != null)
                ? handleOrNull.getTimestamp().getTime()
                : System.currentTimeMillis();

        // L2: Ethernet
        EthernetPacket eth = packet.get(EthernetPacket.class);
        if(eth == null){
            // npr. Linux “cooked capture” može biti drugačije; za MVP samo vrati OTHER
            return new DecodedPacket(ts, null, null, null, null, null, Protocol.OTHER, null, null, new byte[0], TcpFlags.none());
        }

        String srcMac = eth.getHeader().getSrcAddr().toString();
        String dstMac = eth.getHeader().getDstAddr().toString();

        Integer vlanId = null;
        Dot1qVlanTagPacket vlan = packet.get(Dot1qVlanTagPacket.class);
        if(vlan != null){
            vlanId = vlan.getHeader().getVidAsInt();
        }


        //L3 : IPV4 / IPV6
        IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
        IpV6Packet ipV6Packet = packet.get(IpV6Packet.class);

        String srcIp = null;
        String dstIp = null;

        if(ipV4Packet != null){
            srcIp = ipV4Packet.getHeader().getSrcAddr().toString();
            dstIp = ipV4Packet.getHeader().getDstAddr().toString();
        }else if (ipV6Packet != null){
            srcIp = ipV6Packet.getHeader().getSrcAddr().toString();
            dstIp = ipV6Packet.getHeader().getDstAddr().toString();
        }else {
            // nije IP (npr. ARP) → za sada vrati samo L2 info
            return new DecodedPacket(ts, srcMac, dstMac, vlanId, null, null, Protocol.OTHER, null, null, new byte[0], TcpFlags.none());
        }

        TcpPacket tcp = packet.get(TcpPacket.class);
        if(tcp != null){
            int sourcePort = tcp.getHeader().getSrcPort().valueAsInt();
            int destinationPort = tcp.getHeader().getDstPort().valueAsInt();
            byte[] payload = tcp.getPayload() != null ? tcp.getPayload().getRawData() : new byte[0];
            // var jer nam se tip odredjivanje u compile-timeu, a ne u runtimeu
            var h = tcp.getHeader();
            TcpFlags flags = new TcpFlags(
                    h.getSyn(),
                    h.getAck(),
                    h.getFin(),
                    h.getRst(),
                    h.getPsh(),
                    h.getUrg()
            );

            return new DecodedPacket(ts, srcMac, dstMac, vlanId, srcIp, dstIp, Protocol.TCP, sourcePort, destinationPort, payload, flags);
        }

        UdpPacket udp = packet.get(UdpPacket.class);
        if(udp != null){
            int sourcePort = udp.getHeader().getSrcPort().valueAsInt();
            int destinationPort = udp.getHeader().getDstPort().valueAsInt();
            byte[] payload = udp.getPayload() != null ? udp.getPayload().getRawData() : new byte[0];
            return new DecodedPacket(ts, srcMac, dstMac, vlanId, srcIp, dstIp, Protocol.UDP, sourcePort, destinationPort, payload, TcpFlags.none());
        }

        IcmpV4CommonPacket icmp4 = packet.get(IcmpV4CommonPacket.class);
        if (icmp4 != null) {
            byte[] payload = icmp4.getPayload() != null ? icmp4.getPayload().getRawData() : new byte[0];
            return new DecodedPacket(ts, srcMac, dstMac, vlanId, srcIp, dstIp, Protocol.ICMP, null, null, payload, TcpFlags.none());
        }

        IcmpV6CommonPacket icmp6 = packet.get(IcmpV6CommonPacket.class);
        if (icmp6 != null) {
            byte[] payload = icmp6.getPayload() != null ? icmp6.getPayload().getRawData() : new byte[0];
            return new DecodedPacket(ts, srcMac, dstMac, vlanId, srcIp, dstIp, Protocol.ICMP, null, null, payload, TcpFlags.none());
        }

        // Neki drugi L4 proto (GRE/IPsec/OSPF...)
        return new DecodedPacket(ts, srcMac, dstMac, vlanId, srcIp, dstIp, Protocol.OTHER, null, null, new byte[0], TcpFlags.none());

    }
}
