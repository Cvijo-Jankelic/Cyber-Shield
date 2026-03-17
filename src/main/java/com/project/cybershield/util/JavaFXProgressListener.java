package com.project.cybershield.util;

import com.project.cybershield.ui.DownloadProgressPopup;

import java.nio.file.Path;

public class JavaFXProgressListener implements DownloadProgressListener {

    private final DownloadProgressPopup popup;

    public JavaFXProgressListener(DownloadProgressPopup popup) {
        this.popup = popup;
    }

    @Override
    public void onProgress(String description, long downloaded, long total,
                           double percentage, long currentSpeed) {
        // Ažuriraj popup
        popup.updateProgress(description, downloaded, total, percentage, currentSpeed);

        // Provjeri je li otkazano
        if (popup.isCancelled()) {
            throw new RuntimeException("Download cancelled by user");
        }
    }

    @Override
    public void onComplete(String description, Path outputFile) {
        popup.setComplete("Download complete: " + description);
        System.out.println("[INTELLIGENCE] ✓ " + description + " downloaded to " + outputFile);
    }
}
