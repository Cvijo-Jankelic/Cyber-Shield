package com.project.cybershield.ids;

import java.time.LocalDateTime;

public class Packet {
    private String sourceIp;
    private int destinationPort;
    private String protocol;
    private LocalDateTime time;
    private String payload;

    public Packet(String sourceIp, int destinationPort, String protocol, LocalDateTime time, String payload) {
        this.sourceIp = sourceIp;
        this.destinationPort = destinationPort;
        this.protocol = protocol;
        this.time = time;
        this.payload = payload;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "sourceIp='" + sourceIp + '\'' +
                ", destinationPort=" + destinationPort +
                ", protocol='" + protocol + '\'' +
                ", time=" + time +
                ", payload='" + payload + '\'' +
                '}';
    }
}
