package com.project.cybershield.builders;

import com.project.cybershield.entities.Incident;

import java.time.LocalDateTime;

public class IncidentBuilder {
    private Long id;
    private LocalDateTime attackTime;
    private String sourceIP;
    private int destinationPort;
    private Long attackTypeId;
    private int severity;
    private String note;
    private byte[] pcap_data;

    public IncidentBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public IncidentBuilder setAttackTime(LocalDateTime attackTime) {
        this.attackTime = attackTime;
        return this;
    }

    public IncidentBuilder setSourceIP(String sourceIP) {
        this.sourceIP = sourceIP;
        return this;
    }

    public IncidentBuilder setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
        return this;
    }

    public IncidentBuilder setAttackTypeId(Long attackTypeId) {
        this.attackTypeId = attackTypeId;
        return this;
    }

    public IncidentBuilder setSeverity(int severity) {
        this.severity = severity;
        return this;
    }

    public IncidentBuilder setNote(String note) {
        this.note = note;
        return this;
    }

    public IncidentBuilder setPcap_data(byte[] pcap_data) {
        this.pcap_data = pcap_data;
        return this;
    }

    public Incident createIncident() {
        return new Incident(id, attackTime, sourceIP, destinationPort, attackTypeId, severity, note, pcap_data);
    }
}