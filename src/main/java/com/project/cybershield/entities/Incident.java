package com.project.cybershield.entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Incident {
    private Long id;
    private LocalDateTime attackTime;
    private String sourceIP;
    private int destinationPort;
    private Long attackTypeId;
    private int severity;
    private String note;
    private byte[] pcap_data;

    public Incident(Long id, LocalDateTime attackTime, String sourceIP,
                    int destinationPort, Long attackTypeId, int severity, String note, byte[] pcap_Data) {
        this.id = id;
        this.attackTime = attackTime;
        this.sourceIP = sourceIP;
        this.destinationPort = destinationPort;
        this.attackTypeId = attackTypeId;
        this.severity = severity;
        this.note = note;
        this.pcap_data = pcap_data;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAttackTime() {
        return attackTime;
    }

    public void setAttackTime(LocalDateTime attackTime) {
        this.attackTime = attackTime;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public void setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public Long getAttackTypeId() {
        return attackTypeId;
    }

    public void setAttackTypeId(Long attackTypeId) {
        this.attackTypeId = attackTypeId;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public byte[] getPcap_data() {
        return pcap_data;
    }

    public void setPcap_data(byte[] pcap_data) {
        this.pcap_data = pcap_data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Incident incident = (Incident) o;
        return destinationPort == incident.destinationPort && severity == incident.severity
                && Objects.equals(id, incident.id) && Objects.equals(attackTime, incident.attackTime)
                && Objects.equals(sourceIP, incident.sourceIP)
                && Objects.equals(attackTypeId, incident.attackTypeId)
                && Objects.equals(note, incident.note) && Objects.equals(pcap_data, incident.pcap_data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attackTime, sourceIP, destinationPort, attackTypeId, severity, note);
    }

    @Override
    public String toString() {
        return "Incident{" +
                "id=" + id +
                ", attackTime=" + attackTime +
                ", sourceIP='" + sourceIP + '\'' +
                ", destinationPort=" + destinationPort +
                ", attackTypeId=" + attackTypeId +
                ", severity=" + severity +
                ", note='" + note + '\'' +
                '}';
    }
}
