package com.project.cybershield.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AbuseIPDBClient {
    private static final String API_BASE_URL = "https://api.abuseipdb.com/api/v2/";
    private static final String CHECK_ENDPOINT = "/check";

    private String apiKey = "80fa3fc3b826f39ef34dfdab16988ef306da7e879645ae91e3ff39d72ff6836f9d9a5329216e9644";
    private final Gson gson;

    public AbuseIPDBClient() {
        this.gson = new Gson();
    }

    public AbuseIPDBClient(String apiKey) {
        this.apiKey = apiKey;
        this.gson = new Gson();
    }

    /**
     * GLAVNA METODA - Provjerava IP adresu
     *
     * REST POZIV:
     * GET https://api.abuseipdb.com/api/v2/check?ipAddress={ip}&maxAgeInDays=90&verbose
     *
     * @param ipAddress IP adresa za provjeru
     * @return ThreatIntelligenceReport s podacima
     */
    public ThreatIntelligenceReport checkIP(String ipAddress) throws Exception {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("   ABUSEIPDB REST API CALL");
        System.out.println("═══════════════════════════════════════════");
        System.out.println("  IP: " + ipAddress);

        String urlString = String.format("%s%s?ipAddress=%s&maxAgeInDays=90&verbose",
                API_BASE_URL,
                CHECK_ENDPOINT,
                ipAddress
        );
        System.out.println("  URL: " + urlString);
        System.out.println("  METHOD: GET");

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Key", apiKey); // API Key authentication
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        System.out.println("  Headers: Key=*****, Accept=application/json");

        // Šalji zahtjev
        int responseCode = conn.getResponseCode();
        System.out.println("  Response Code: " + responseCode);

        if (responseCode != 200) {
            throw new Exception("API Error: HTTP " + responseCode);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        String jsonResponse = response.toString();
        System.out.println("  Response Size: " + jsonResponse.length() + " bytes");
        System.out.println("═══════════════════════════════════════════\n");

        // Parsiraj JSON
        return parseResponse(jsonResponse);

    }

    private ThreatIntelligenceReport parseResponse(String jsonResponse) {
        JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);
        JsonObject data = root.getAsJsonObject("data");

        ThreatIntelligenceReport report = new ThreatIntelligenceReport();

        // Osnovni podaci
        report.ipAddress = data.get("ipAddress").getAsString();
        report.isPublic = data.get("isPublic").getAsBoolean();
        report.ipVersion = data.get("ipVersion").getAsInt();

        // THREAT INTELLIGENCE
        report.abuseConfidenceScore = data.get("abuseConfidenceScore").getAsInt();
        report.totalReports = data.get("totalReports").getAsInt();
        report.numDistinctUsers = data.get("numDistinctUsers").getAsInt();
        report.isWhitelisted = data.get("isWhitelisted").getAsBoolean();

        // Zadnja aktivnost
        if (!data.get("lastReportedAt").isJsonNull()) {
            report.lastReportedAt = data.get("lastReportedAt").getAsString();
        }

        // Lokacija i ISP
        if (!data.get("countryCode").isJsonNull()) {
            report.countryCode = data.get("countryCode").getAsString();
        }
        if (!data.get("countryName").isJsonNull()) {
            report.countryName = data.get("countryName").getAsString();
        }
        if (!data.get("isp").isJsonNull()) {
            report.isp = data.get("isp").getAsString();
        }
        if (!data.get("domain").isJsonNull()) {
            report.domain = data.get("domain").getAsString();
        }
        if (!data.get("usageType").isJsonNull()) {
            report.usageType = data.get("usageType").getAsString();
        }

        // Kategorije napada (ako postoje)
        if (data.has("reports") && !data.get("reports").isJsonNull()) {
            JsonArray reports = data.getAsJsonArray("reports");
            List<String> categories = new ArrayList<>();

            for (int i = 0; i < reports.size(); i++) {
                JsonObject reportObj = reports.get(i).getAsJsonObject();
                JsonArray cats = reportObj.getAsJsonArray("categories");

                for (int j = 0; j < cats.size(); j++) {
                    int categoryId = cats.get(j).getAsInt();
                    String categoryName = getCategoryName(categoryId);
                    if (!categories.contains(categoryName)) {
                        categories.add(categoryName);
                    }
                }
            }

            report.attackCategories = categories;
        }

        // Threat level (izračunaj iz abuse score)
        report.threatLevel = calculateThreatLevel(report.abuseConfidenceScore);

        return report;
    }

    /**
     * Mapira category ID u ljudski čitljiv naziv
     */
    private String getCategoryName(int categoryId) {
        switch (categoryId) {
            case 3: return "Fraud Orders";
            case 4: return "DDoS Attack";
            case 5: return "FTP Brute-Force";
            case 6: return "Ping of Death";
            case 7: return "Phishing";
            case 8: return "Fraud VoIP";
            case 9: return "Open Proxy";
            case 10: return "Web Spam";
            case 11: return "Email Spam";
            case 12: return "Blog Spam";
            case 13: return "VPN IP";
            case 14: return "Port Scan";
            case 15: return "Hacking";
            case 16: return "SQL Injection";
            case 17: return "Spoofing";
            case 18: return "Brute-Force";
            case 19: return "Bad Web Bot";
            case 20: return "Exploited Host";
            case 21: return "Web App Attack";
            case 22: return "SSH Brute-Force";
            case 23: return "IoT Targeted";
            default: return "Unknown (" + categoryId + ")";
        }
    }
    private String calculateThreatLevel(int abuseScore) {
        if (abuseScore >= 75) return "CRITICAL";
        if (abuseScore >= 50) return "HIGH";
        if (abuseScore >= 25) return "MEDIUM";
        if (abuseScore > 0) return "LOW";
        return "CLEAN";
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
