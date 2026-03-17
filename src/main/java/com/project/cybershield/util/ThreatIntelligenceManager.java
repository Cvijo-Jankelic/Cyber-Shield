package com.project.cybershield.util;

import com.project.cybershield.enums.SpeedLimit;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ThreatIntelligenceManager {
    private final ThreatIntelligenceDownloader downloader;
    private final Set<String> blacklistedIps = ConcurrentHashMap.newKeySet();

    private static final String EMERGING_THREATS_IP_LIST =
            "https://rules.emergingthreats.net/blockrules/compromised-ips.txt";

    private static final String ABUSE_IP_DB_LIST =
            "https://raw.githubusercontent.com/stamparm/ipsum/master/ipsum.txt";

    public ThreatIntelligenceManager(Path downloadDir) {
        this.downloader = new ThreatIntelligenceDownloader(
                downloadDir,
                new ConsoleProgressListener()
        );
    }

    public void updateThreatFeeds() throws Exception {
        System.out.println("[INTELLIGENCE] Starting threat feed update...");

        // Download IP blacklist
        downloader.setSpeedLimit(SpeedLimit.MEDIUM);
        downloader.downloadIpBlackList(
                EMERGING_THREATS_IP_LIST,
                "emerging-threats-ips.txt"
        );

        // Load into memory
        Set<String> newIps = downloader.loadIpBlacklist("emerging-threats-ips.txt");
        blacklistedIps.clear();
        blacklistedIps.addAll(newIps);

        System.out.println("[INTELLIGENCE] Threat feeds updated successfully");
    }

    public boolean isBlacklisted(String ip) {
        return blacklistedIps.contains(ip);
    }

}
