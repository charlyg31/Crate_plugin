package com.hazebyte.crate.api;

import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerVersion implements Comparable<ServerVersion> {

    private static final Map<String, ServerVersion> versions = new HashMap<>();

    private static final Pattern numberPattern = Pattern.compile("[0-9]+\\.[0-9]+\\.[0-9]+");
    private static final Pattern altNumberPattern = Pattern.compile("[0-9]+\\.[0-9]+");

    public static ServerVersion v1_8_R1 = new ServerVersion(1, 8, 1);
    public static ServerVersion v1_8_R2 = new ServerVersion(1, 8, 2);
    public static ServerVersion v1_9_R1 = new ServerVersion(1, 9, 1);
    public static ServerVersion v1_10_R1 = new ServerVersion(1, 10, 1);
    public static ServerVersion v1_12_R1 = new ServerVersion(1, 12, 1);
    public static ServerVersion v1_13_R1 = new ServerVersion(1, 13, 1);
    public static ServerVersion v1_14_R1 = new ServerVersion(1, 14, 1);
    public static ServerVersion v1_16_R1 = new ServerVersion(1, 16, 1);
    public static ServerVersion v1_18_R0 = new ServerVersion(1, 18, 0);
    public static ServerVersion v1_20_R6 = new ServerVersion(1, 20, 6);
    public static ServerVersion v1_21_R0 = new ServerVersion(1, 21, 0);
    public static ServerVersion v26_1_R0 = new ServerVersion(26, 1, 0);

    public static ServerVersion SERVER_MOCK = new ServerVersion(Integer.MAX_VALUE, 0, 0);

    private final int major;
    private final int minor;
    private final int revision;

    private ServerVersion(int major, int minor, int revision) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
    }

    public static boolean isMockServer(String versionString) {
        return versionString.contains("1.19");
    }

    public boolean isMockServer() {
        return this.equals(SERVER_MOCK);
    }

    public static ServerVersion of(String versionString) {
        if (isMockServer(versionString)) {
            return ServerVersion.SERVER_MOCK;
        }

        // Extract X.Y.Z from any format like "26.1.2.build.68"
        Matcher m3 = numberPattern.matcher(versionString);
        if (m3.find()) {
            versionString = m3.group();
        } else {
            Matcher m2 = altNumberPattern.matcher(versionString);
            if (m2.find()) {
                versionString = m2.group();
            } else {
                throw new IllegalArgumentException(
                    String.format("Unable to parse server version: [%s]", versionString));
            }
        }

        if (versions.containsKey(versionString))
            return versions.get(versionString);

        String[] parts = versionString.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int revision = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        ServerVersion version = new ServerVersion(major, minor, revision);
        versions.put(versionString, version);
        return version;
    }

    public static ServerVersion getVersion() {
        String serverVersion = Bukkit.getServer().getBukkitVersion();
        return ServerVersion.of(serverVersion);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(major) + Integer.hashCode(minor) + Integer.hashCode(revision);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ServerVersion)) return false;
        return this.compareTo((ServerVersion) obj) == 0;
    }

    @Override
    public String toString() {
        return String.format("%d_%d_R%d", major, minor, revision);
    }

    public boolean gt(ServerVersion version) { return this.compareTo(version) > 0; }
    public boolean gte(ServerVersion version) { return this.compareTo(version) >= 0; }
    public boolean lt(ServerVersion version) { return this.compareTo(version) < 0; }
    public boolean lte(ServerVersion version) { return this.compareTo(version) <= 0; }

    @Override
    public int compareTo(ServerVersion serverVersion) {
        if (this.major != serverVersion.major) return Integer.compare(this.major, serverVersion.major);
        if (this.minor != serverVersion.minor) return Integer.compare(this.minor, serverVersion.minor);
        return Integer.compare(this.revision, serverVersion.revision);
    }

    public int getMajor() { return major; }
    public int getMinor() { return minor; }
    public int getRevision() { return revision; }
}
