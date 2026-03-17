package com.project.cybershield.util;

import java.nio.file.Path;

public interface DownloadProgressListener {
    void onProgress(String description, long downloaded, long total,
                    double percentage, long currentSpeed);
    void onComplete(String description, Path outputFile);
}
