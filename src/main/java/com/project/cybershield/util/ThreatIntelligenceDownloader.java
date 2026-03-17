package com.project.cybershield.util;

import com.project.cybershield.enums.SpeedLimit;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class ThreatIntelligenceDownloader {
    private final Path downloadDir;
    private final DownloadProgressListener progressListener;

    private SpeedLimit currentSpeedLimit = SpeedLimit.SLOW;
    private final AtomicBoolean downloadActive = new AtomicBoolean(false);


    public ThreatIntelligenceDownloader(Path downloadDir, DownloadProgressListener progressListener) {
        this.downloadDir = downloadDir;
        this.progressListener = progressListener;

        try {
            Files.createDirectories(downloadDir);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Cannot create download directory" + ex.getMessage());
            throw new RuntimeException("Cannot create download directory" + ex.getMessage());
        }
    }

    public void downloadIpBlackList(String sourceUrl, String fileName) throws IOException {
        download(sourceUrl, fileName, "Signature Rule");
    }

    //downloadActive = false
    //
    //compareAndSet(false, true) → true thread safety vraca ako je vrijednost == expected value vraca newvalue
    //
    //!true → false
    //
    //if se ne izvršava
    //
    //download se normalno pokrene
    private void download(String sourceUrl, String fileName, String description) throws IOException {
        if (!downloadActive.compareAndSet(false, true)) {
            throw new RuntimeException("There is already an active download");
        }

        Path outputFile = downloadDir.resolve(fileName);

        try {
            URL url = new URL(sourceUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Get content length for progress calculation
            long totalBytes = conn.getContentLengthLong();

            System.out.println("[HTTP] Downloading: " + description);
            System.out.println("[HTTP] Source: " + sourceUrl);
            System.out.println("[HTTP] Size: " + formatBytes(totalBytes));
            System.out.println("[HTTP] Speed limit: " + currentSpeedLimit);

            // Progress tracking
            AtomicLong downloadedBytes = new AtomicLong(0);
            long startTime = System.currentTimeMillis();

            try (InputStream in = conn.getInputStream();
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(outputFile))) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long lastProgressUpdate = System.currentTimeMillis();
                long lastSpeedCheckTime = System.currentTimeMillis();
                long bytesInCurrentSecond = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);

                    long downloaded = downloadedBytes.addAndGet(bytesRead);
                    bytesInCurrentSecond += bytesRead;

                    // Progress update every 500ms
                    long now = System.currentTimeMillis();
                    if (now - lastProgressUpdate >= 500) {
                        double percentage = totalBytes > 0
                                ? (downloaded * 100.0 / totalBytes)
                                : 0;

                        long elapsedSeconds = (now - startTime) / 1000;
                        long currentSpeed = elapsedSeconds > 0
                                ? downloaded / elapsedSeconds
                                : 0;

                        // Notify listener
                        if (progressListener != null) {
                            progressListener.onProgress(
                                    description,
                                    downloaded,
                                    totalBytes,
                                    percentage,
                                    currentSpeed
                            );
                        }

                        // Console output
                        System.out.printf("[HTTP] Progress: %.1f%% (%s / %s) @ %s/s%n",
                                percentage,
                                formatBytes(downloaded),
                                formatBytes(totalBytes),
                                formatBytes(currentSpeed));

                        lastProgressUpdate = now;
                    }

                    // SPEED LIMITING
                    if (currentSpeedLimit != SpeedLimit.UNLIMITED) {
                        long elapsedInSecond = now - lastSpeedCheckTime;

                        if (elapsedInSecond >= 1000) {
                            // Reset for next second
                            lastSpeedCheckTime = now;
                            bytesInCurrentSecond = 0;
                        } else if (bytesInCurrentSecond >= currentSpeedLimit.bytesPerSecond) {
                            // Sleep to throttle speed
                            long sleepTime = 1000 - elapsedInSecond;
                            if (sleepTime > 0) {
                                try {
                                    Thread.sleep(sleepTime);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw new IOException("Download interrupted", e);
                                }
                            }
                            lastSpeedCheckTime = System.currentTimeMillis();
                            bytesInCurrentSecond = 0;
                        }
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            long totalTime = (endTime - startTime) / 1000;
            long avgSpeed = totalTime > 0 ? totalBytes / totalTime : 0;

            System.out.println("[HTTP] ✓ Download complete!");
            System.out.printf("[HTTP] Total time: %d seconds, Avg speed: %s/s%n",
                    totalTime, formatBytes(avgSpeed));
            System.out.println("[HTTP] Saved to: " + outputFile);

            // Notify completion
            if (progressListener != null) {
                progressListener.onComplete(description, outputFile);
            }

        } finally {
            downloadActive.set(false);
        }
    }

    public void setSpeedLimit(SpeedLimit speedLimit) {
        this.currentSpeedLimit = speedLimit;
        System.out.println("[HTTP] SpeedLimit changed to: " + speedLimit);
    }

    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
    public Set<String> loadIpBlacklist(String filename) throws IOException {
        Path file = downloadDir.resolve(filename);
        Set<String> blacklist = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip comments and empty lines
                if (!line.isEmpty() && !line.startsWith("#")) {
                    blacklist.add(line);
                }
            }
        }

        System.out.println("[INTELLIGENCE] Loaded " + blacklist.size() + " blacklisted IPs");
        return blacklist;
    }


}
