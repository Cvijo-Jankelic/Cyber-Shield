package com.project.cybershield.api;

import java.util.List;

public class ThreatIntelligenceReport {
    public String ipAddress;
    public boolean isPublic;
    public int ipVersion;

    // Threat Intelligence
    public int abuseConfidenceScore;  // 0-100% (koliko je IP maliciozan)
    public int totalReports;          // Broj prijava
    public int numDistinctUsers;      // Broj različitih reportera
    public boolean isWhitelisted;     // Je li na whitelisti
    public String lastReportedAt;     // Zadnja prijava
    public String threatLevel;        // CLEAN, LOW, MEDIUM, HIGH, CRITICAL

    // Lokacija i ISP
    public String countryCode;
    public String countryName;
    public String isp;
    public String domain;
    public String usageType;

    // Kategorije napada
    public List<String> attackCategories;

    /**
     * Je li IP maliciozan?
     */
    public boolean isMalicious() {
        return abuseConfidenceScore > 0;
    }

    /**
     * Je li visokorizična prijetnja?
     */
    public boolean isHighThreat() {
        return abuseConfidenceScore >= 50;
    }

    /**
     * Ljudski čitljiv summary
     */
    public String getSummary() {
        if (abuseConfidenceScore == 0) {
            return "No abuse reports found - IP appears clean";
        }

        return String.format(
                "%s threat - %d%% confidence, reported %d times by %d users",
                threatLevel,
                abuseConfidenceScore,
                totalReports,
                numDistinctUsers
        );
    }
}
