package com.project.cybershield.pcapStorage;

import com.project.cybershield.entities.Incident;
import com.project.cybershield.repository.IncidentRepo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class PcapStorageService {
    private final IncidentRepo incidentRepo;

    public PcapStorageService(IncidentRepo incidentRepo) {
        this.incidentRepo = incidentRepo;
    }

    public Incident attachPcapData(Incident incident, byte[] pcapData) {
        incident.setPcap_data(pcapData);
        return incident;
    }

    public byte[] readPcapFile(Path pcapFile) throws IOException {
        return Files.readAllBytes(pcapFile);
    }

    public Optional<byte[]> loadPcapData(long incidentId) {
        return incidentRepo.findPcapDataByIncidentId(incidentId);
    }

    public Path exportPcapData(long incidentId, Path outputFile) throws IOException {
        byte[] pcapData = loadPcapData(incidentId)
                .orElseThrow(() -> new IOException("No PCAP data found for incident id=" + incidentId));

        if (outputFile.getParent() != null) {
            Files.createDirectories(outputFile.getParent());
        }

        Files.write(outputFile, pcapData);
        return outputFile;
    }
}
