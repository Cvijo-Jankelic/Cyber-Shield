package com.project.cybershield.auth;

public record Parsed(int iterations, byte[] salt, byte[] hash, int keyLengthBits) {
}
