package com.project.cybershield.ids;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.project.cybershield.entities.Protocol;

import java.util.regex.Pattern;

public class SignaturePayload {
    private String id;
    private String name;
    private Protocol protocol;
    private Integer dstPort;
    private Integer srcPort;
    private String regex;
    private String regexFlags;
    private String contains;
    private Integer severity;
    private String description;

    public SignaturePayload() {}


    public SignaturePayload(String id, String name, Protocol protocol, Integer dstPort, Integer srcPort, String regex, String regexFlags, String contains,
                            Integer severity, String description) {
        this.id = id;
        this.name = name;
        this.protocol = protocol;
        this.dstPort = dstPort;
        this.srcPort = srcPort;
        this.regex = regex;
        this.regexFlags = regexFlags;
        this.contains = contains;
        this.severity = severity;
        this.description = description;
    }

    @JsonIgnore
    private Pattern compiledRegex;

    public Pattern getCompiledRegex() {
        if (compiledRegex == null && regex != null) {
            int flags = 0;

            if (regexFlags != null) {
                if (regexFlags.contains("i")) flags |= Pattern.CASE_INSENSITIVE;
                if (regexFlags.contains("m")) flags |= Pattern.MULTILINE;
                if (regexFlags.contains("s")) flags |= Pattern.DOTALL;
            }

            compiledRegex = Pattern.compile(regex, flags);
        }
        return compiledRegex;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public String getRegex() {
        return regex;
    }

    public String getRegexFlags() {
        return regexFlags;
    }

    public String getContains() {
        return contains;
    }

    public Integer getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }
}
