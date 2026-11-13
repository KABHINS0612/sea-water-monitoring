package com.seawater.monitoring.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Blynk IoT Cloud Configuration properties.
 * Binds properties from application.properties under the prefix 'blynk'.
 */
@Configuration
@ConfigurationProperties(prefix = "blynk")
public class BlynkConfig {

    private String server = "blynk.cloud";
    private String authToken = "YOUR_BLYNK_AUTH_TOKEN";
    private String templateId = "TMPL3qzRJhyC8";
    private String templateName = "sea water monitering";
    private boolean wasteInverted = false;

    private Pins pins = new Pins();
    private Gauge gauge = new Gauge();
    private Http http = new Http();

    public static class Pins {
        private String temperature = "v0";
        private String waste = "v1";
        private String humidity = "v2";
        private String water = "v3";

        public String getTemperature() { return temperature; }
        public void setTemperature(String temperature) { this.temperature = temperature; }

        public String getWaste() { return waste; }
        public void setWaste(String waste) { this.waste = waste; }

        public String getHumidity() { return humidity; }
        public void setHumidity(String humidity) { this.humidity = humidity; }

        public String getWater() { return water; }
        public void setWater(String water) { this.water = water; }
    }

    public static class Gauge {
        private Range temperature = new Range(0.0, 200.0);
        private Range humidity = new Range(0.0, 100.0);
        private Range water = new Range(0.0, 500.0);

        public static class Range {
            private double min;
            private double max;

            public Range() {}
            public Range(double min, double max) {
                this.min = min;
                this.max = max;
            }

            public double getMin() { return min; }
            public void setMin(double min) { this.min = min; }

            public double getMax() { return max; }
            public void setMax(double max) { this.max = max; }
        }

        public Range getTemperature() { return temperature; }
        public void setTemperature(Range temperature) { this.temperature = temperature; }

        public Range getHumidity() { return humidity; }
        public void setHumidity(Range humidity) { this.humidity = humidity; }

        public Range getWater() { return water; }
        public void setWater(Range water) { this.water = water; }
    }

    public static class Http {
        private int connectTimeout = 4000;
        private int readTimeout = 5000;

        public int getConnectTimeout() { return connectTimeout; }
        public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }

        public int getReadTimeout() { return readTimeout; }
        public void setReadTimeout(int readTimeout) { this.readTimeout = readTimeout; }
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(http.getConnectTimeout());
        factory.setReadTimeout(http.getReadTimeout());
        return new RestTemplate(factory);
    }

    // Getters and Setters
    public String getServer() { return server; }
    public void setServer(String server) { this.server = server; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public boolean isWasteInverted() { return wasteInverted; }
    public void setWasteInverted(boolean wasteInverted) { this.wasteInverted = wasteInverted; }

    public Pins getPins() { return pins; }
    public void setPins(Pins pins) { this.pins = pins; }

    public Gauge getGauge() { return gauge; }
    public void setGauge(Gauge gauge) { this.gauge = gauge; }

    public Http getHttp() { return http; }
    public void setHttp(Http http) { this.http = http; }
}

