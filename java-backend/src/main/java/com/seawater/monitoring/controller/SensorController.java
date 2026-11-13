package com.seawater.monitoring.controller;

import com.seawater.monitoring.model.BlynkStatus;
import com.seawater.monitoring.model.SensorData;
import com.seawater.monitoring.service.BlynkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller exposing IoT sensor telemetry endpoints.
 * All endpoints fetch live readings directly from Blynk IoT Cloud.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SensorController {

    private final BlynkService blynkService;

    public SensorController(BlynkService blynkService) {
        this.blynkService = blynkService;
    }

    /**
     * GET /api/sensors
     * Returns all four sensor readings (temperature, waste, humidity, water),
     * connectivity status, timestamps, and gauge ranges in JSON format.
     */
    @GetMapping("/sensors")
    public ResponseEntity<SensorData> getAllSensors() {
        SensorData data = blynkService.getSensorData();
        return ResponseEntity.ok(data);
    }

    /**
     * GET /api/sensors/temperature
     * Returns the live temperature reading.
     */
    @GetMapping("/sensors/temperature")
    public ResponseEntity<Map<String, Object>> getTemperature() {
        SensorData data = blynkService.getSensorData();
        Map<String, Object> response = new HashMap<>();
        response.put("sensor", "temperature");
        response.put("value", data.getTemperature());
        response.put("unit", "°C");
        response.put("pin", data.getPins().get("temperature"));
        response.put("min", data.getGaugeRanges().get("temperature").getMin());
        response.put("max", data.getGaugeRanges().get("temperature").getMax());
        response.put("status", data.getTemperature() != null ? "AVAILABLE" : "UNAVAILABLE");
        response.put("timestamp", data.getTimestamp());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/sensors/humidity
     * Returns the live humidity reading.
     */
    @GetMapping("/sensors/humidity")
    public ResponseEntity<Map<String, Object>> getHumidity() {
        SensorData data = blynkService.getSensorData();
        Map<String, Object> response = new HashMap<>();
        response.put("sensor", "humidity");
        response.put("value", data.getHumidity());
        response.put("unit", "%");
        response.put("pin", data.getPins().get("humidity"));
        response.put("min", data.getGaugeRanges().get("humidity").getMin());
        response.put("max", data.getGaugeRanges().get("humidity").getMax());
        response.put("status", data.getHumidity() != null ? "AVAILABLE" : "UNAVAILABLE");
        response.put("timestamp", data.getTimestamp());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/sensors/waste
     * Returns the waste detection status (circular indicator state).
     */
    @GetMapping("/sensors/waste")
    public ResponseEntity<Map<String, Object>> getWaste() {
        SensorData data = blynkService.getSensorData();
        Map<String, Object> response = new HashMap<>();
        response.put("sensor", "waste");
        response.put("detected", data.getWasteDetected());
        response.put("status", data.getWasteStatus()); // "WASTE DETECTED" or "NO WASTE DETECTED"
        response.put("rawValue", data.getWasteRawValue());
        response.put("pin", data.getPins().get("waste"));
        response.put("timestamp", data.getTimestamp());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/sensors/water
     * Returns the water level reading.
     */
    @GetMapping("/sensors/water")
    public ResponseEntity<Map<String, Object>> getWater() {
        SensorData data = blynkService.getSensorData();
        Map<String, Object> response = new HashMap<>();
        response.put("sensor", "water");
        response.put("value", data.getWater());
        response.put("unit", "raw/level");
        response.put("pin", data.getPins().get("water"));
        response.put("min", data.getGaugeRanges().get("water").getMin());
        response.put("max", data.getGaugeRanges().get("water").getMax());
        response.put("status", data.getWater() != null ? "AVAILABLE" : "UNAVAILABLE");
        response.put("timestamp", data.getTimestamp());
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/blynk/status
     * Returns Blynk connectivity status (ONLINE or OFFLINE, latency, hardware state).
     */
    @GetMapping("/blynk/status")
    public ResponseEntity<BlynkStatus> getBlynkStatus() {
        BlynkStatus status = blynkService.getBlynkStatus();
        return ResponseEntity.ok(status);
    }

    /**
     * GET /api/blynk/config
     * Returns general project metadata (template ID, pin mappings, gauge bounds).
     * Auth token is masked for security.
     */
    @GetMapping("/blynk/config")
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("templateId", blynkService.getConfig().getTemplateId());
        configMap.put("templateName", blynkService.getConfig().getTemplateName());
        configMap.put("server", blynkService.getConfig().getServer());
        configMap.put("tokenConfigured", blynkService.isTokenConfigured());
        configMap.put("pins", blynkService.getConfig().getPins());
        configMap.put("gauge", blynkService.getConfig().getGauge());
        return ResponseEntity.ok(configMap);
    }

    /**
     * POST /api/blynk/config
     * Allows updating the Auth Token and/or pin mappings from the UI settings modal.
     */
    @PostMapping("/blynk/config")
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, String> payload) {
        String token = payload.get("authToken");
        String server = payload.get("server");
        String pinTemp = payload.get("pinTemperature");
        String pinWaste = payload.get("pinWaste");
        String pinHumi = payload.get("pinHumidity");
        String pinWater = payload.get("pinWater");

        if (token != null && !token.trim().isEmpty()) {
            blynkService.updateCredentials(token, server);
        }
        blynkService.updatePinMappings(pinTemp, pinWaste, pinHumi, pinWater);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Blynk configuration updated successfully.");
        result.put("status", blynkService.getBlynkStatus());
        return ResponseEntity.ok(result);
    }
}

