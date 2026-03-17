package com.project.cybershield.repository;

import com.project.cybershield.builders.FirewallRuleBuilder;
import com.project.cybershield.builders.IncidentBuilder;
import com.project.cybershield.db.DatabaseConfig;
import com.project.cybershield.entities.FirewallRule;
import com.project.cybershield.entities.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentRepo {
    private static final Logger logger = LoggerFactory.getLogger(IncidentRepo.class);


    public void insert(Incident incident){

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            String sql = "INSERT INTO incidents (attack_time, source_ip, destination_port, attack_type_id, severity, note) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1, Timestamp.valueOf(incident.getAttackTime()));
            ps.setString(2, incident.getSourceIP());
            ps.setInt(3, incident.getDestinationPort());
            ps.setLong(4, incident.getAttackTypeId());
            ps.setInt(5, incident.getSeverity());
            ps.setString(6, incident.getNote());
            int affected = ps.executeUpdate();

            System.out.println("Rows inserted: " + affected);

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error inserting incident in database: id = {} ", incident.getAttackTypeId(), ex);
        }
    }

    public List<Incident> getAll(){
        List<Incident> firewallRuleList = new ArrayList<>();

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM incidents");
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

                firewallRuleList.add(incident);
                System.out.println("Successfully fetched Incidents from database!");
            }

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error fetching incidents dataset from database: ", ex);
        }

        return firewallRuleList;
    }

    public void addScreenshotToIncident(int incidentId, byte[] image) {
        try (Connection con = DatabaseConfig.connectionToDatabase();
             PreparedStatement ps = con.prepareStatement("UPDATE incidents SET screenshot_blob=? WHERE id=?")) {

            ps.setBytes(1, image);
            ps.setInt(2, incidentId);

            ps.executeUpdate();

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error pushing screenshot to database", ex);
        }
    }
}
