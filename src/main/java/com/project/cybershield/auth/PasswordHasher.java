package com.project.cybershield.auth;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Objects;

/**
 * PASSWORD HASHER sa DETERMINISTIČKI SOL i PAPAR
 *
 * SAŽIMANJE: PBKDF2-HMAC-SHA256 (120,000 iteracija)
 *
 * SOL (PROMJENJIVA, ali deterministička):
 *   - Generira se iz username-a: sol = SHA-256(username)
 *   - NE SPREMA SE u bazu
 *   - Pri verifikaciji se regenerira iz username-a
 *   - Svaki user ima različitu sol jer različit username
 *
 * PAPAR (FIKSNI):
 *   - Tajni string poznat samo serveru
 *   - Čuva se u konfiguraciji (NE u bazi!)
 *   - Kombinira se sa passwordom prije hashiranja
 *   - Provjera kroz iteraciju svih mogućih vrijednosti
 */
public final class PasswordHasher {

    private static final int DEFAULT_ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final String KDF_ALG = "PBKDF2WithHmacSHA256";

    // PAPAR - moguće vrijednosti (samo jedna je ispravna)
    private static final String[] POSSIBLE_PEPPERS = {
            "WrongPepper123",
            "AnotherFakePepper",
            "IncorrectSecret",
            "CyberShield2026!SecretPepper", // ← ISPRAVNI PAPAR (index 3)
            "YetAnotherWrongOne"
    };

    private static final int CORRECT_PEPPER_INDEX = 3;

    private PasswordHasher() {}

    /**
     * Hash password sa deterministički sol (iz username-a) i paprom
     *
     * @param plainPassword - plaintext lozinka
     * @param username - username (koristi se za generiranje soli)
     * @return Base64-encoded hash (BEZ soli - sol se ne sprema!)
     */
    public static String hash(String plainPassword, String username)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        validatePassword(plainPassword);
        validateUsername(username);

        // 1. Generiraj SOL iz username-a (deterministički!)
        byte[] salt = generateSaltFromUsername(username);

        // 2. Dodaj PAPAR password-u
        String pepper = POSSIBLE_PEPPERS[CORRECT_PEPPER_INDEX];
        String passwordWithPepper = plainPassword + pepper;

        // 3. Hash sa PBKDF2
        byte[] derived = pbkdf2(
                passwordWithPepper.toCharArray(),
                salt,
                DEFAULT_ITERATIONS,
                KEY_LENGTH_BITS
        );

        // 4. Vrati samo hash (BEZ soli - sol se ne sprema!)
        String hashB64 = Base64.getEncoder().encodeToString(derived);

        // Format: pbkdf2$sha256$iterations$hash (bez soli!)
        return "pbkdf2$sha256$" + DEFAULT_ITERATIONS + "$" + hashB64;
    }

    /**
     * Verificiraj password
     *
     * DEMONSTRACIJA PROVJERE PAPRA:
     * - Prolazi kroz SVE moguće papre
     * - Ispisuje koji papar se testira
     * - Samo jedan papar će dati ispravan hash
     */
    public static boolean verify(String plainPassword, String username, String storedHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (plainPassword == null || username == null || storedHash == null) {
            return false;
        }

        // Parse stored hash
        Parsed parsed;
        try {
            parsed = parse(storedHash);
        } catch (IllegalArgumentException ex) {
            System.err.println("[AUTH] Invalid hash format: " + ex.getMessage());
            return false;
        }

        // Generiraj istu sol iz username-a (deterministički!)
        byte[] salt = generateSaltFromUsername(username);

        // PROVJERA KROZ SVE MOGUĆE PAPRE
        System.out.println("[AUTH] Verifying password for user: " + username);
        System.out.println("[AUTH] Testing all possible peppers...");

        for (int i = 0; i < POSSIBLE_PEPPERS.length; i++) {
            String pepper = POSSIBLE_PEPPERS[i];
            String passwordWithPepper = plainPassword + pepper;

            // Hash sa ovim paprom
            byte[] candidateHash = pbkdf2(
                    passwordWithPepper.toCharArray(),
                    salt,
                    parsed.iterations(),
                    parsed.keyLengthBits()
            );

            // Provjera
            boolean matches = MessageDigest.isEqual(candidateHash, parsed.hash());

            System.out.printf("[AUTH]   Pepper[%d] (%s...): %s%n",
                    i,
                    pepper.substring(0, Math.min(15, pepper.length())),
                    matches ? "✓ MATCH!" : "✗ no match"
            );

            if (matches) {
                System.out.println("[AUTH] ✓ Password verified successfully!");
                return true;
            }
        }

        System.out.println("[AUTH] ✗ Password verification failed (no pepper matched)");
        return false;
    }

    /**
     * Generiraj SOL iz username-a (DETERMINISTIČKI)
     *
     * Pravilo: sol = SHA-256(username)
     *
     * Zašto ovo radi:
     *   - Isti username = ista sol (može se regenerirati)
     *   - Različiti username = različita sol (svaki user unique)
     *   - Ne treba spremati sol u bazu
     *   - Sol se regenerira pri login-u iz username-a
     */
    private static byte[] generateSaltFromUsername(String username)
            throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(username.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * PBKDF2 key derivation
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(KDF_ALG);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Parse stored hash string
     * Format: pbkdf2$sha256$iterations$hash (BEZ soli!)
     */
    private static Parsed parse(String stored) {
        String[] parts = stored.split("\\$");

        // Očekujemo 4 dijela: pbkdf2$sha256$iterations$hash
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid stored hash format (expected 4 parts)");
        }

        if (!"pbkdf2".equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported scheme: " + parts[0]);
        }

        if (!"sha256".equals(parts[1])) {
            throw new IllegalArgumentException("Unsupported digest: " + parts[1]);
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid iterations: " + parts[2]);
        }
        validateIterations(iterations);

        byte[] hash;
        try {
            hash = Base64.getDecoder().decode(parts[3]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 hash");
        }

        int keyLenBits = hash.length * 8;

        // Sol nije u hash-u - ona se regenerira iz username-a!
        return new Parsed(iterations, null, hash, keyLenBits);
    }

    /**
     * Check if password needs rehashing (higher iterations)
     */
    public static boolean needsRehash(String storedHash, int desiredIterations) {
        validateIterations(desiredIterations);

        try {
            Parsed parsed = parse(storedHash);
            return parsed.iterations() < desiredIterations;
        } catch (IllegalArgumentException ex) {
            return true; // Invalid hash = needs rehash
        }
    }

    // ==================== VALIDATION ====================

    private static void validatePassword(String p) {
        Objects.requireNonNull(p, "Password must not be null");
        if (p.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (p.length() > 1024) {
            throw new IllegalArgumentException("Password too long (max 1024 chars)");
        }
    }

    private static void validateUsername(String username) {
        Objects.requireNonNull(username, "Username must not be null");
        if (username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (username.length() < 3) {
            throw new IllegalArgumentException("Username too short (min 3 chars)");
        }
    }

    private static void validateIterations(int iterations) {
        if (iterations < 10_000 || iterations > 5_000_000) {
            throw new IllegalArgumentException(
                    "Iterations out of range (10k-5M): " + iterations
            );
        }
    }

    // ==================== PARSED RECORD ====================

    /**
     * Internal record for parsed hash components
     * NOTE: salt is null (regenerirano iz username-a)
     */
    private record Parsed(int iterations, byte[] salt, byte[] hash, int keyLengthBits) {}
}