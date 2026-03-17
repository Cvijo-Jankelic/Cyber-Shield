package com.project.cybershield.builders;

import com.project.cybershield.entities.FirewallRule;

import java.time.LocalDateTime;

public class FirewallRuleBuilder {
    private Long id;
    private String ruleName;
    private String action;
    private String ip;
    private int port;
    private LocalDateTime createdAt;
    private boolean enable;

    public FirewallRuleBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public FirewallRuleBuilder setRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    public FirewallRuleBuilder setAction(String action) {
        this.action = action;
        return this;
    }

    public FirewallRuleBuilder setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public FirewallRuleBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public FirewallRuleBuilder setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public FirewallRuleBuilder setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    public FirewallRule createFirewallRule() {
        return new FirewallRule(id, ruleName, action, ip, port, createdAt, enable);
    }
}