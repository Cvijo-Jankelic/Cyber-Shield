package com.project.cybershield.repository;

import com.project.cybershield.builders.AttackTypeBuilder;
import com.project.cybershield.db.DatabaseConfig;
import com.project.cybershield.entities.AttackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttackTypeRepo {

    private static final Logger logger = LoggerFactory.getLogger(AttackTypeRepo.class);


    public void insert(AttackType attackType){

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            String sql = "INSERT INTO attack_types (name, default_severity, description) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, attackType.getName());
            ps.setInt(2, attackType.getDefaultSeverity());
            ps.setString(3, attackType.getDescription());
            int affected = ps.executeUpdate();

            System.out.println("Rows inserted: " + affected);

        } catch (SQLException  | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error inserting attack type in database: {}", attackType.getName(), ex);
        }
    }

    public List<AttackType> getAll(){
        List<AttackType> attackTypeList = new ArrayList<>();

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM attack_types");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                AttackType at = new AttackTypeBuilder()
                        .setId(rs.getLong("id"))
                        .setName(rs.getString("name"))
                        .setDefaultSeverity(rs.getInt("default_severity"))
                        .setDescription(rs.getString("description"))
                        .createAttackType();

                attackTypeList.add(at);
            }
            System.out.println("Successfully fetched AttackTypes from database!");
        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error fetching attackType data from database: ", ex);
        }

        return attackTypeList;
    }



}
