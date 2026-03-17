package com.project.cybershield.builders;

import com.project.cybershield.entities.AttackType;

public class AttackTypeBuilder {
    private Long id;
    private String name;
    private int defaultSeverity;
    private String description;

    public AttackTypeBuilder setId(Long id) {
        this.id = id;
        return this;
    }

    public AttackTypeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public AttackTypeBuilder setDefaultSeverity(int defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
        return this;
    }

    public AttackTypeBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public AttackType createAttackType() {
        return new AttackType(id, name, defaultSeverity, description);
    }
}