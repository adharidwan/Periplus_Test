package com.periplus.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "https://www.periplus.com";
    private static final String DEFAULT_SEARCH_TERM = "atomic habits";
    private static final String DEFAULT_EXPECTED_PRODUCT = "Atomic Habits";
    private static final int DEFAULT_TIMEOUT_SECONDS = 25;
    private static final Map<String, String> DOTENV_VALUES = loadDotEnvFile();

    private final String baseUrl;
    private final String email;
    private final String password;
    private final String searchTerm;
    private final String expectedProduct;
    private final boolean headless;
    private final Duration timeout;

    private TestConfig(
            String baseUrl,
            String email,
            String password,
            String searchTerm,
            String expectedProduct,
            boolean headless,
            Duration timeout) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.email = email;
        this.password = password;
        this.searchTerm = searchTerm;
        this.expectedProduct = expectedProduct;
        this.headless = headless;
        this.timeout = timeout;
    }

    public static TestConfig fromEnvironment() {
        int timeoutSeconds = parseInt(read("periplus.timeoutSeconds", "PERIPLUS_TIMEOUT_SECONDS", ""), DEFAULT_TIMEOUT_SECONDS);

        return new TestConfig(
                read("periplus.baseUrl", "PERIPLUS_BASE_URL", DEFAULT_BASE_URL),
                read("periplus.email", "PERIPLUS_EMAIL", ""),
                read("periplus.password", "PERIPLUS_PASSWORD", ""),
                read("periplus.searchTerm", "PERIPLUS_SEARCH_TERM", DEFAULT_SEARCH_TERM),
                read("periplus.expectedProduct", "PERIPLUS_EXPECTED_PRODUCT", DEFAULT_EXPECTED_PRODUCT),
                Boolean.parseBoolean(read("periplus.headless", "PERIPLUS_HEADLESS", "false")),
                Duration.ofSeconds(timeoutSeconds));
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String email() {
        return email;
    }

    public String password() {
        return password;
    }

    public String searchTerm() {
        return searchTerm;
    }

    public String expectedProduct() {
        return expectedProduct;
    }

    public boolean headless() {
        return headless;
    }

    public Duration timeout() {
        return timeout;
    }

    private static String read(String propertyName, String environmentName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (hasText(propertyValue)) {
            return propertyValue.trim();
        }

        String environmentValue = System.getenv(environmentName);
        if (hasText(environmentValue)) {
            return environmentValue.trim();
        }

        String dotenvValue = DOTENV_VALUES.get(environmentName);
        if (hasText(dotenvValue)) {
            return dotenvValue.trim();
        }

        return defaultValue;
    }

    private static Map<String, String> loadDotEnvFile() {
        Path dotEnvPath = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.exists(dotEnvPath)) {
            return Map.of();
        }

        Map<String, String> values = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(dotEnvPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!hasText(trimmedLine) || trimmedLine.startsWith("#") || !trimmedLine.contains("=")) {
                    continue;
                }

                int separatorIndex = trimmedLine.indexOf('=');
                String key = trimmedLine.substring(0, separatorIndex).trim();
                String value = trimmedLine.substring(separatorIndex + 1).trim();
                if (hasText(key)) {
                    values.put(key, stripQuotes(value));
                }
            }
        } catch (IOException ignored) {
            return Map.of();
        }

        return Map.copyOf(values);
    }

    private static int parseInt(String rawValue, int defaultValue) {
        if (!hasText(rawValue)) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String stripTrailingSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1);
            }
        }

        return value;
    }
}
