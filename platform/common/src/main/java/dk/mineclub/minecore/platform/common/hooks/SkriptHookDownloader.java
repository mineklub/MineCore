package dk.mineclub.minecore.platform.common.hooks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.plugin.Plugin;

/** Downloads the appropriate Skript hook JAR for the current Java version. */
public abstract class SkriptHookDownloader {
    private static final String DOWNLOAD_URL_BASE =
            "http://r2plugins.mineclub.dk/MineCore/hooks/{skriptVersion}-{classifier}.jar";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();

    protected final Plugin plugin;
    private final List<HookVersionRange> hookVersionRanges = new ArrayList<>();

    protected SkriptHookDownloader(Plugin plugin) {
        this.plugin = plugin;
        initializeSkriptVersions();
    }

    /**
     * Initialize available Skript versions with their supported Java versions and version ranges.
     * Priority is from highest to lowest (first entry is preferred).
     */
    private void initializeSkriptVersions() {
        // Maps hook versions to their supported version ranges and Java versions
        hookVersionRanges.add(
                new HookVersionRange(
                        "skript22dev36", "2.2", "2.5.999", new int[] {8, 11, 17, 21, 25}));
        hookVersionRanges.add(
                new HookVersionRange(
                        "skript295", "2.6.4", "2.12.999", new int[] {8, 11, 17, 21, 25}));
        hookVersionRanges.add(
                new HookVersionRange("skript213", "2.13", "2.14.999", new int[] {17, 21, 25}));
        hookVersionRanges.add(
                new HookVersionRange("skript", "2.15", "2.999.999", new int[] {17, 21, 25}));
    }

    /**
     * Downloads the best available Skript hook for the current Java version if it doesn't already
     * exist. Respects the Skript plugin version if already present.
     *
     * @param hooksDir the directory where hooks should be stored
     */
    public void downloadIfNeeded(Path hooksDir) {
        int javaVersion = detectJavaFeatureVersion();
        int classifier = getJvmClassifier(javaVersion);

        if (classifier == 1) {
            plugin.getLogger()
                    .warning(
                            "Skript hook not available for Java version "
                                    + javaVersion
                                    + ". Supported Java versions: 8, 11, 17, 21, 25");
            return;
        }

        // Check for existing Skript plugin and determine compatible hook versions
        String skriptPluginVersion = findSkriptPluginVersion(hooksDir);
        if (skriptPluginVersion == null) {
            plugin.getLogger().info("No Skript plugin detected. ");
            return;
        }

        String bestSkriptVersion = findBestSkriptVersion(classifier, skriptPluginVersion);

        if (bestSkriptVersion == null) {
            plugin.getLogger()
                    .warning(
                            "No Skript version available for Java "
                                    + javaVersion
                                    + ". Supported Java versions: 8, 11, 17, 21, 25");
            return;
        }

        String fileName = "Skript-hook.jar";
        Path hookFile = hooksDir.resolve(fileName);

        downloadHook(bestSkriptVersion, classifier, hookFile);
    }

    /**
     * Finds the Skript plugin version using platform-specific implementation.
     *
     * @param hooksDir the directory where hooks should be stored
     * @return the Skript version (e.g., "2.14.3") or null if not found
     */
    protected abstract String findSkriptPluginVersion(Path hooksDir);

    /**
     * Finds the best available Skript version for the given Java version. Priority is based on the
     * order in hookVersionRanges list (highest priority first). If a Skript plugin version is
     * specified, only returns hook versions compatible with that version.
     *
     * @param javaVersion the Java major version
     * @param skriptPluginVersion the Skript plugin version (e.g., "2.14.3") or null
     * @return the best Skript version, or null if none available
     */
    private String findBestSkriptVersion(int javaVersion, String skriptPluginVersion) {
        for (HookVersionRange range : hookVersionRanges) {
            // Check if this Java version is supported
            if (Arrays.stream(range.supportedJavaVersions).noneMatch(v -> v == javaVersion)) {
                continue;
            }

            // If Skript plugin version is specified, check if it falls within the hook's range
            if (skriptPluginVersion != null) {
                if (isVersionInRange(skriptPluginVersion, range.minVersion, range.maxVersion)) {
                    return range.hookName;
                }
            } else {
                return range.hookName;
            }
        }
        return null;
    }

    /**
     * Checks if a version falls within the specified range. Uses semantic versioning comparison.
     *
     * @param version the version to check (e.g., "2.14.3", "2.15.0-dev")
     * @param minVersion the minimum version (inclusive)
     * @param maxVersion the maximum version (inclusive)
     * @return true if version is within range, false otherwise
     */
    private boolean isVersionInRange(String version, String minVersion, String maxVersion) {
        return compareVersions(version, minVersion) >= 0
                && compareVersions(version, maxVersion) <= 0;
    }

    /**
     * Compares two semantic versions. Returns: negative if v1 < v2, 0 if v1 == v2, positive if v1 >
     * v2 Handles formats like "2.14.3", "2.15.0-dev", "2.9.5", etc.
     *
     * @param v1 the first version
     * @param v2 the second version
     * @return comparison result
     */
    private int compareVersions(String v1, String v2) {
        try {
            String[] parts1 = v1.split("[.-]");
            String[] parts2 = v2.split("[.-]");

            int maxLength = Math.max(parts1.length, parts2.length);

            for (int i = 0; i < maxLength; i++) {
                int num1 =
                        (i < parts1.length && isNumeric(parts1[i]))
                                ? Integer.parseInt(parts1[i])
                                : 0;
                int num2 =
                        (i < parts2.length && isNumeric(parts2[i]))
                                ? Integer.parseInt(parts2[i])
                                : 0;

                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            }
            return 0;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to compare versions: " + v1 + " vs " + v2);
            return 0;
        }
    }

    /**
     * Checks if a string is numeric.
     *
     * @param str the string to check
     * @return true if numeric, false otherwise
     */
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void downloadHook(String skriptVersion, int classifier, Path targetPath) {
        String downloadUrl =
                DOWNLOAD_URL_BASE
                        .replace("{skriptVersion}", skriptVersion)
                        .replace("{classifier}", "jvm" + classifier);

        try {
            plugin.getLogger()
                    .info("Downloading Skript hook: " + skriptVersion + " for Java " + classifier);
            plugin.getLogger().info("from: " + downloadUrl);

            Request request = new Request.Builder().url(downloadUrl).build();

            try (Response response = HTTP_CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("HTTP error code: " + response.code());
                }

                if (response.body() == null) {
                    throw new IOException("Empty response body");
                }

                try (OutputStream output = Files.newOutputStream(targetPath)) {
                    output.write(response.body().bytes());
                }
            }

            plugin.getLogger()
                    .info("Successfully downloaded Skript hook to: " + targetPath.toAbsolutePath());
        } catch (IOException ex) {
            plugin.getLogger()
                    .warning(
                            "Failed to download Skript hook "
                                    + targetPath.getFileName()
                                    + ": "
                                    + ex.getMessage());
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException ignored) {
                // Best effort cleanup
            }
        }
    }

    private static int detectJavaFeatureVersion() {
        String specVersion = System.getProperty("java.specification.version", "").trim();
        if (specVersion.isEmpty()) {
            return 8;
        }

        if (specVersion.startsWith("1.")) {
            specVersion = specVersion.substring(2);
        }

        try {
            return Integer.parseInt(specVersion);
        } catch (NumberFormatException ignored) {
            return 8;
        }
    }

    private static int getJvmClassifier(int javaFeature) {
        if (javaFeature >= 25) {
            return 25;
        }
        if (javaFeature >= 21) {
            return 21;
        }
        if (javaFeature >= 17) {
            return 17;
        }
        if (javaFeature >= 11) {
            return 11;
        }
        if (javaFeature >= 8) {
            return 8;
        }
        return 1;
    }

    /** Internal class representing a hook version range with supported Java versions. */
    private static class HookVersionRange {
        final String hookName;
        final String minVersion;
        final String maxVersion;
        final int[] supportedJavaVersions;

        HookVersionRange(
                String hookName,
                String minVersion,
                String maxVersion,
                int[] supportedJavaVersions) {
            this.hookName = hookName;
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
            this.supportedJavaVersions = supportedJavaVersions;
        }
    }
}
