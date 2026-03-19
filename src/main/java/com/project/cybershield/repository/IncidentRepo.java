package com.project.cybershield.repository;

import com.project.cybershield.builders.IncidentBuilder;
import com.project.cybershield.db.DatabaseConfig;
import com.project.cybershield.entities.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IncidentRepo {
    private static final Logger logger = LoggerFactory.getLogger(IncidentRepo.class);


    public void insert(Incident incident){

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            String sql = "INSERT INTO incidents (attack_time, source_ip, destination_port, attack_type_id, severity, note, pcap_data) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(incident.getAttackTime()));
            ps.setString(2, incident.getSourceIP());
            ps.setInt(3, incident.getDestinationPort());
            ps.setLong(4, incident.getAttackTypeId());
            ps.setInt(5, incident.getSeverity());
            ps.setString(6, incident.getNote());
            ps.setBytes(7, normalizePcapData(incident.getPcap_data()));
            int affected = ps.executeUpdate();

            logger.info("Inserted incident rows={}", affected);

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error inserting incident in database: id = {} ", incident.getAttackTypeId(), ex);
        }
    }

    public List<Incident> getAll(){
        List<Incident> incidents = new ArrayList<>();

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            PreparedStatement ps = conn.prepareStatement("""
                    SELECT id, attack_time, source_ip, destination_port, attack_type_id, severity, note
                    FROM incidents
                    ORDER BY attack_time DESC
                    """);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                Incident incident = new IncidentBuilder()
                        .setId(rs.getLong("id"))
                        .setAttackTime(rs.getTimestamp("attack_time").toLocalDateTime())
                        .setSourceIP(rs.getString("source_ip"))
                        .setDestinationPort(rs.getInt("destination_port"))
                        .setAttackTypeId(rs.getLong("attack_type_id"))
                        .setSeverity(rs.getInt("severity"))
                        .setNote(rs.getString("note"))
                        .createIncident();

                incidents.add(incident);
            }

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error fetching incidents dataset from database: ", ex);
        }

        return incidents;
    }

    public Optional<byte[]> findPcapDataByIncidentId(long incidentId) {
        try (Connection con = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = con.prepareStatement("SELECT pcap_data FROM incidents WHERE id = ?")) {
            ps.setLong(1, incidentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.ofNullable(rs.getBytes("pcap_data"));
            }

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error fetching PCAP data for incident id={}", incidentId, ex);
            return Optional.empty();
        }
    }

    private byte[] normalizePcapData(byte[] pcapData) {
        return pcapData != null ? pcapData : new byte[0];
    }
}
