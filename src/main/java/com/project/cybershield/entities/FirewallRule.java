package com.project.cybershield.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class FirewallRule {
    private Long id;
    private String ruleName;
    private String action;
    private String ip;
    private int port;
    private LocalDateTime createdAt;
    private boolean enable = true;

    public FirewallRule(Long id, String ruleName, String action, String ip,
                        int port, LocalDateTime createdAt, boolean enable) {
        this.id = id;
        this.ruleName = ruleName;
        this.action = action;
        this.ip = ip;
        this.port = port;
        this.createdAt = createdAt;
        this.enable = enable;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FirewallRule that = (FirewallRule) o;
        return id == that.id && port == that.port && enable == that.enable && Objects.equals(ruleName, that.ruleName) && Objects.equals(action, that.action) && Objects.equals(ip, that.ip) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ruleName, action, ip, port, createdAt, enable);
    }

    @Override
    public String toString() {
        return "FirewallRule{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", action='" + action + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", createdAt=" + createdAt +
                ", enable=" + enable +
                '}';
    }
}
