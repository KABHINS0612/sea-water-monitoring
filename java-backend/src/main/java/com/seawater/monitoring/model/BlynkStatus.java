package com.seawater.monitoring.model;

import java.time.Instant;

/**
 * Model representing Blynk IoT Cloud connection and hardware status.
 */
public class BlynkStatus {

    private boolean online;
    private String status;               // "ONLINE" or "OFFLINE"
    private boolean hardwareConnected;   // ESP32 hardware connection to Blynk
    private boolean cloudReachable;      // Spring Boot backend can reach Blynk REST API
    private String server;
    private String templateId;
    private String templateName;
    private boolean tokenConfigured;
    private String lastSync;
    private long latencyMs;
    private String message;

    public BlynkStatus() {
        this.lastSync = Instant.now().toString();
    }

    public BlynkStatus(boolean online, String status, boolean hardwareConnected, String message) {
        this.online = online;
        this.status = status;
        this.hardwareConnected = hardwareConnected;
        this.message = message;
        this.lastSync = Instant.now().toString();
    }

    // Getters and Setters
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isHardwareConnected() { return hardwareConnected; }
    public void setHardwareConnected(boolean hardwareConnected) { this.hardwareConnected = hardwareConnected; }

    public boolean isCloudReachable() { return cloudReachable; }
    public void setCloudReachable(boolean cloudReachable) { this.cloudReachable = cloudReachable; }

    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public boolean isTokenConfigured() { return tokenConfigured; }
    public void setTokenConfigured(boolean tokenConfigured) { this.tokenConfigured = tokenConfigured; }

    public String getLastSync() { return lastSync; }
    public void setLastSync(String lastSync) { this.lastSync = lastSync; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

