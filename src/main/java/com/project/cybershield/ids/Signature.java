package com.project.cybershield.ids;

public class Signature {
    private String name;
    private String pattern;
    private Integer severity;
    private Integer minPorts;
    private Integer maxPorts;
    private String description;

    public Signature(String name, String pattern, Integer severity, Integer minPorts, Integer maxPorts, String description) {
        this.name = name;
        this.pattern = pattern;
        this.severity = severity;
        this.minPorts = minPorts;
        this.maxPorts = maxPorts;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getPattern() {
        return pattern;
    }

    public Integer getSeverity() {
        return severity;
    }

    public Integer getMinPorts() {
        return minPorts;
    }

    public Integer getMaxPorts() {
        return maxPorts;
    }

    public String getDescription() {
        return description;
    }
}
