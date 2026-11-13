package com.seawater.monitoring.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing all four sensor readings from Blynk IoT Cloud.
 * Cards:
 *   1. Temperature (temp): Semicircular gauge (0 - 200)
 *   2. Waste Detection (waste): Circular indicator (Detected / Clear)
 *   3. Humidity (humi): Semicircular gauge (0 - 100)
 *   4. Water Level (water): Semicircular gauge (0 - 500)
 */
public class SensorData {

    // Numerical & State Readings
    private Double temperature;      // °C
    private Double humidity;         // %
    private Double water;            // Water Level (e.g., cm / mm / raw)
    private Boolean wasteDetected;   // true = Waste Detected (Alert), false = No Waste (Normal)
    private String wasteStatus;      // "WASTE DETECTED" or "NO WASTE DETECTED"
    private String wasteRawValue;    // Raw string from Blynk pin

    // Metadata & Connection
    private boolean dataAvailable;
    private boolean hardwareConnected;
    private String blynkStatus;      // "ONLINE" or "OFFLINE"
    private String timestamp;
    private String errorMessage;

    // Pin mappings and gauge ranges sent to frontend
    private Map<String, String> pins = new HashMap<>();
    private Map<String, GaugeRange> gaugeRanges = new HashMap<>();

    public static class GaugeRange {
        private double min;
        private double max;

        public GaugeRange() {}
        public GaugeRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() { return min; }
        public void setMin(double min) { this.min = min; }

        public double getMax() { return max; }
        public void setMax(double max) { this.max = max; }
    }

    public SensorData() {
        this.timestamp = Instant.now().toString();
        this.dataAvailable = false;
        this.wasteStatus = "NOT DETECTED";
        this.wasteDetected = false;
    }

    // Getters and Setters
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }
    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public Double getWater() { return water; }
    public void setWater(Double water) { this.water = water; }

    public Boolean getWasteDetected() { return wasteDetected; }
    public void setWasteDetected(Boolean wasteDetected) { this.wasteDetected = wasteDetected; }

    public String getWasteStatus() { return wasteStatus; }
    public void setWasteStatus(String wasteStatus) { this.wasteStatus = wasteStatus; }

    public String getWasteRawValue() { return wasteRawValue; }
    public void setWasteRawValue(String wasteRawValue) { this.wasteRawValue = wasteRawValue; }

    public boolean isDataAvailable() { return dataAvailable; }
    public void setDataAvailable(boolean dataAvailable) { this.dataAvailable = dataAvailable; }

    public boolean isHardwareConnected() { return hardwareConnected; }
    public void setHardwareConnected(boolean hardwareConnected) { this.hardwareConnected = hardwareConnected; }

    public String getBlynkStatus() { return blynkStatus; }
    public void setBlynkStatus(String blynkStatus) { this.blynkStatus = blynkStatus; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, String> getPins() { return pins; }
    public void setPins(Map<String, String> pins) { this.pins = pins; }

    public Map<String, GaugeRange> getGaugeRanges() { return gaugeRanges; }
    public void setGaugeRanges(Map<String, GaugeRange> gaugeRanges) { this.gaugeRanges = gaugeRanges; }
}

