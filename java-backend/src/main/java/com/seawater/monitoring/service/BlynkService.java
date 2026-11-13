package com.seawater.monitoring.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seawater.monitoring.config.BlynkConfig;
import com.seawater.monitoring.model.BlynkStatus;
import com.seawater.monitoring.model.SensorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to communicate directly with Blynk IoT Cloud REST API.
 * Handles hardware status checks, pin value retrieval, batch and fallback requests,
 * and data mapping for the Sea Water Monitoring System.
 */
@Service
public class BlynkService {

    private static final Logger log = LoggerFactory.getLogger(BlynkService.class);

    private final BlynkConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BlynkService(BlynkConfig config, RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Checks if Blynk Auth Token is configured and not the default placeholder.
     */
    public boolean isTokenConfigured() {
        String token = config.getAuthToken();
        return token != null && !token.trim().isEmpty() && !token.equalsIgnoreCase("YOUR_BLYNK_AUTH_TOKEN");
    }

    /**
     * Retrieves connectivity and hardware status from Blynk Cloud.
     * Endpoint: https://{server}/external/api/isHardwareConnected?token={token}
     */
    public BlynkStatus getBlynkStatus() {
        BlynkStatus status = new BlynkStatus();
        status.setServer(config.getServer());
        status.setTemplateId(config.getTemplateId());
        status.setTemplateName(config.getTemplateName());
        status.setTokenConfigured(isTokenConfigured());

        if (!isTokenConfigured()) {
            status.setOnline(false);
            status.setStatus("OFFLINE");
            status.setHardwareConnected(false);
            status.setCloudReachable(false);
            status.setMessage("Blynk Auth Token is not configured. Please set 'blynk.auth-token' in application.properties or via dashboard.");
            return status;
        }

        long startTime = System.currentTimeMillis();
        String url = String.format("https://%s/external/api/isHardwareConnected?token=%s",
                config.getServer(), config.getAuthToken().trim());

        try {
            String response = restTemplate.getForObject(url, String.class);
            long latency = System.currentTimeMillis() - startTime;
            status.setLatencyMs(latency);
            status.setCloudReachable(true);

            boolean isHardwareOnline = response != null && response.trim().equalsIgnoreCase("true");
            status.setHardwareConnected(isHardwareOnline);

            if (isHardwareOnline) {
                status.setOnline(true);
                status.setStatus("ONLINE");
                status.setMessage("Device is connected to Blynk Cloud and actively transmitting.");
            } else {
                status.setOnline(false);
                status.setStatus("OFFLINE");
                status.setMessage("Blynk Cloud is reachable, but the ESP32 hardware device is currently offline.");
            }
        } catch (Exception e) {
            log.error("Failed to connect to Blynk Cloud API: {}", e.getMessage());
            status.setOnline(false);
            status.setStatus("OFFLINE");
            status.setHardwareConnected(false);
            status.setCloudReachable(false);
            status.setMessage("Unable to reach Blynk Cloud API: " + e.getMessage());
        }

        return status;
    }

    /**
     * Fetches all four sensor readings from Blynk Cloud.
     * Uses batch multi-pin GET first, and falls back to individual pin calls if needed.
     */
    public SensorData getSensorData() {
        SensorData data = new SensorData();
        populateMetadata(data);

        if (!isTokenConfigured()) {
            data.setDataAvailable(false);
            data.setBlynkStatus("OFFLINE");
            data.setHardwareConnected(false);
            data.setErrorMessage("Blynk Auth Token not configured in backend.");
            return data;
        }

        BlynkStatus status = getBlynkStatus();
        data.setHardwareConnected(status.isHardwareConnected());
        data.setBlynkStatus(status.getStatus());

        String pinTemp = normalizePin(config.getPins().getTemperature());
        String pinWaste = normalizePin(config.getPins().getWaste());
        String pinHumi = normalizePin(config.getPins().getHumidity());
        String pinWater = normalizePin(config.getPins().getWater());

        Map<String, String> pinValues = new HashMap<>();
        boolean fetchedBatch = false;

        // Attempt 1: Multi-pin batch request (e.g. get?token=...&v0&v1&v2&v3)
        try {
            String batchUrl = String.format("https://%s/external/api/get?token=%s&%s&%s&%s&%s",
                    config.getServer(), config.getAuthToken().trim(), pinTemp, pinWaste, pinHumi, pinWater);

            String response = restTemplate.getForObject(batchUrl, String.class);
            if (response != null && response.trim().startsWith("{")) {
                JsonNode root = objectMapper.readTree(response);
                root.fields().forEachRemaining(entry -> {
                    pinValues.put(entry.getKey().toLowerCase(), entry.getValue().asText());
                });
                fetchedBatch = true;
            }
        } catch (Exception e) {
            log.debug("Batch pin fetch not supported or failed, falling back to individual calls: {}", e.getMessage());
        }

        // Attempt 2: Fallback to individual pin fetching if batch did not populate
        if (!fetchedBatch) {
            pinValues.put(pinTemp, fetchSinglePin(pinTemp));
            pinValues.put(pinWaste, fetchSinglePin(pinWaste));
            pinValues.put(pinHumi, fetchSinglePin(pinHumi));
            pinValues.put(pinWater, fetchSinglePin(pinWater));
        }

        // Map Temperature (Card 1)
        String rawTemp = pinValues.get(pinTemp);
        if (rawTemp != null && !rawTemp.equalsIgnoreCase("null") && !rawTemp.isEmpty()) {
            try {
                data.setTemperature(Double.parseDouble(rawTemp.trim().replace("\"", "")));
            } catch (NumberFormatException e) {
                log.warn("Invalid temperature number from pin {}: {}", pinTemp, rawTemp);
            }
        }

        // Map Humidity (Card 3)
        String rawHumi = pinValues.get(pinHumi);
        if (rawHumi != null && !rawHumi.equalsIgnoreCase("null") && !rawHumi.isEmpty()) {
            try {
                data.setHumidity(Double.parseDouble(rawHumi.trim().replace("\"", "")));
            } catch (NumberFormatException e) {
                log.warn("Invalid humidity number from pin {}: {}", pinHumi, rawHumi);
            }
        }

        // Map Water Level (Card 4)
        String rawWater = pinValues.get(pinWater);
        if (rawWater != null && !rawWater.equalsIgnoreCase("null") && !rawWater.isEmpty()) {
            try {
                data.setWater(Double.parseDouble(rawWater.trim().replace("\"", "")));
            } catch (NumberFormatException e) {
                log.warn("Invalid water level number from pin {}: {}", pinWater, rawWater);
            }
        }

        // Map Waste Detection (Card 2)
        String rawWaste = pinValues.get(pinWaste);
        data.setWasteRawValue(rawWaste != null ? rawWaste.trim().replace("\"", "") : "0");
        boolean detected = parseWasteState(rawWaste, config.isWasteInverted());
        data.setWasteDetected(detected);
        data.setWasteStatus(detected ? "WASTE DETECTED" : "NO WASTE DETECTED");

        // Validate overall data availability
        boolean hasAnyReading = data.getTemperature() != null || data.getHumidity() != null
                || data.getWater() != null || rawWaste != null;

        data.setDataAvailable(hasAnyReading);
        if (!hasAnyReading) {
            data.setErrorMessage("No active sensor data available on virtual pins.");
        }

        return data;
    }

    /**
     * Fetches single pin value from Blynk Cloud.
     */
    public String fetchSinglePin(String pin) {
        if (!isTokenConfigured()) return null;
        String cleanPin = normalizePin(pin);
        String url = String.format("https://%s/external/api/get?token=%s&%s",
                config.getServer(), config.getAuthToken().trim(), cleanPin);
        try {
            String val = restTemplate.getForObject(url, String.class);
            return val != null ? val.trim().replace("\"", "") : null;
        } catch (Exception e) {
            log.warn("Failed fetching pin {}: {}", cleanPin, e.getMessage());
            return null;
        }
    }

    /**
     * Parses waste sensor reading into boolean state.
     */
    private boolean parseWasteState(String raw, boolean inverted) {
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String clean = raw.trim().replace("\"", "").toUpperCase();
        boolean isHigh;

        if (clean.equals("1") || clean.equals("HIGH") || clean.equals("TRUE") || clean.equals("ON") || clean.equals("DETECTED")) {
            isHigh = true;
        } else if (clean.equals("0") || clean.equals("LOW") || clean.equals("FALSE") || clean.equals("OFF")) {
            isHigh = false;
        } else {
            try {
                double val = Double.parseDouble(clean);
                isHigh = val > 0.5; // analog or digital threshold
            } catch (Exception e) {
                isHigh = false;
            }
        }

        return inverted != isHigh;
    }

    private String normalizePin(String pin) {
        if (pin == null) return "v0";
        return pin.trim().toLowerCase();
    }

    private void populateMetadata(SensorData data) {
        // Pins
        Map<String, String> pins = new HashMap<>();
        pins.put("temperature", config.getPins().getTemperature());
        pins.put("waste", config.getPins().getWaste());
        pins.put("humidity", config.getPins().getHumidity());
        pins.put("water", config.getPins().getWater());
        data.setPins(pins);

        // Configurable Gauge Ranges
        Map<String, SensorData.GaugeRange> ranges = new HashMap<>();
        ranges.put("temperature", new SensorData.GaugeRange(
                config.getGauge().getTemperature().getMin(),
                config.getGauge().getTemperature().getMax()
        ));
        ranges.put("humidity", new SensorData.GaugeRange(
                config.getGauge().getHumidity().getMin(),
                config.getGauge().getHumidity().getMax()
        ));
        ranges.put("water", new SensorData.GaugeRange(
                config.getGauge().getWater().getMin(),
                config.getGauge().getWater().getMax()
        ));
        data.setGaugeRanges(ranges);
    }

    /**
     * Allows dynamic update of Auth Token & Server without restarting.
     */
    public synchronized void updateCredentials(String token, String server) {
        if (token != null && !token.trim().isEmpty()) {
            config.setAuthToken(token.trim());
        }
        if (server != null && !server.trim().isEmpty()) {
            config.setServer(server.trim());
        }
    }

    /**
     * Allows dynamic update of Virtual Pin assignments.
     */
    public synchronized void updatePinMappings(String tempPin, String wastePin, String humiPin, String waterPin) {
        if (tempPin != null && !tempPin.trim().isEmpty()) config.getPins().setTemperature(tempPin.trim());
        if (wastePin != null && !wastePin.trim().isEmpty()) config.getPins().setWaste(wastePin.trim());
        if (humiPin != null && !humiPin.trim().isEmpty()) config.getPins().setHumidity(humiPin.trim());
        if (waterPin != null && !waterPin.trim().isEmpty()) config.getPins().setWater(waterPin.trim());
    }

    public BlynkConfig getConfig() {
        return config;
    }
}

