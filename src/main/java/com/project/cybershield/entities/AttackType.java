package com.project.cybershield.entities;

import java.util.Objects;

public class AttackType {
    private Long id;
    private String name;
    private int defaultSeverity;
    private String description;

    public AttackType(Long id, String name, int defaultSeverity, String description) {
        this.id = id;
        this.name = name;
        this.defaultSeverity = defaultSeverity;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDefaultSeverity() {
        return defaultSeverity;
    }

    public void setDefaultSeverity(int defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AttackType that = (AttackType) o;
        return id == that.id && defaultSeverity == that.defaultSeverity && Objects.equals(name, that.name) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, defaultSeverity, description);
    }

    @Override
    public String toString() {
        return "AttackType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", defaultSeverity=" + defaultSeverity +
                ", description='" + description + '\'' +
                '}';
    }
}
