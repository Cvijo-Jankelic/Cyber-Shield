package com.project.cybershield.repository;

import com.project.cybershield.builders.AttackTypeBuilder;
import com.project.cybershield.builders.FirewallRuleBuilder;
import com.project.cybershield.db.DatabaseConfig;
import com.project.cybershield.entities.AttackType;
import com.project.cybershield.entities.FirewallRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FirewallRuleRepo {
    private static final Logger logger = LoggerFactory.getLogger(FirewallRuleRepo.class);


    public void insert(FirewallRule firewallRule){

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            String sql = "INSERT INTO firewall_rules (rule_name, action, ip, port, created_at, enable) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, firewallRule.getRuleName());
            ps.setString(2, firewallRule.getAction());
            ps.setString(3, firewallRule.getIp());
            ps.setInt(4, firewallRule.getPort());
            ps.setTimestamp(5, Timestamp.valueOf(firewallRule.getCreatedAt()));
            ps.setBoolean(6, firewallRule.isEnable());
            int affected = ps.executeUpdate();

            System.out.println("Rows inserted: " + affected);

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error inserting firewall_rule in database: {}", firewallRule.getRuleName(), ex);
        }
    }

    public List<FirewallRule> getAll(){
        List<FirewallRule> firewallRuleList = new ArrayList<>();

        try(Connection conn = DatabaseConfig.connectionToDatabase()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM attack_types");
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                FirewallRule fw = new FirewallRuleBuilder()
                        .setId(rs.getLong("id"))
                        .setRuleName(rs.getString("rule_name"))
                        .setAction(rs.getString("action"))
                        .setIp(rs.getString("ip"))
                        .setPort(rs.getInt("port"))
                        .setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime())
                        .setEnable(rs.getBoolean("enable"))
                                .createFirewallRule();

                firewallRuleList.add(fw);
                System.out.println("Successfully fetched FirewallRules from database!");
            }

        } catch (SQLException | IOException ex) {
            logger.info("Error with connection check log file");
            logger.error("Error fetching attackType data from database: ", ex);
        }

        return firewallRuleList;
    }
}
