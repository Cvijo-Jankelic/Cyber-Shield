package com.project.cybershield.util;

import java.nio.file.Path;

public class ConsoleProgressListener implements DownloadProgressListener {

    @Override
    public void onProgress(String description, long downloaded, long total,
                           double percentage, long currentSpeed) {
        // Already printed in downloader
    }

    @Override
    public void onComplete(String description, Path outputFile) {
        System.out.println("[INTELLIGENCE] ✓ " + description + " downloaded");
    }
}

