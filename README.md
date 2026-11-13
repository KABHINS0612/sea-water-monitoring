# Sea Water Monitoring System – Executive Luxury IoT Suite

A comprehensive, executive-grade **Sea Water Monitoring System** featuring a **Java 17 Spring Boot REST API Backend** and a multi-page **Node.js Luxury Marine Frontend**.

Connected directly to **Blynk IoT Cloud** (`TMPL3qzRJhyC8` / `sea water monitering`).

---

## JAVA BACKEND SETUP

```bash
cd java-backend
mvn spring-boot:run
```

Access: `http://localhost:8080/api/sensors`

---

## FRONTEND SETUP

```bash
cd frontend
npm run dev
```

*(Zero dependencies required &bull; starts immediately)*

Access: `http://localhost:3000`

---

## 5 Luxury Dashboard Pages

You can navigate between all pages via the executive top navigation bar:

| Page | URL Path | Luxury Design & Capabilities |
| :--- | :--- | :--- |
| **🌊 Live Monitor** | `/index.html` | Core telemetry dashboard featuring 3 Semicircular Vector Gauges (Temperature, Humidity, Water Level) and the Marine Sonar Beacon (Waste Detection). |
| **📈 Analytics** | `/analytics.html` | Real-time time-series telemetry curves with Chart.js, KPI highlight tiles (Peak Temp, Tide Elevation, Humidity Index), and CSV telemetry export. |
| **🚨 Incident Logs** | `/alerts.html` | Chronological environmental alarm log, severity filtering (Critical, Warning, Info), and optical contamination event tracker. |
| **⚡ Hardware** | `/devices.html` | ESP32-WROOM-32 hardware specifications, live 4-bar Wi-Fi RSSI monitor, Virtual Pin topology table, and live terminal transaction stream. |
| **⚙️ Configuration** | `/settings.html` | Blynk Auth Token management (securely stored on backend), Server Cluster selection, and gauge min/max boundary calibration. |

---

## 4 Core Sensor Monitoring Cards (Blynk Mapping)

1. **Card 1: Temperature (`temp`)** &rarr; Virtual Pin **V0** (Semicircular SVG Gauge: 0 to 200 °C)
2. **Card 2: Waste Detection (`waste`)** &rarr; Virtual Pin **V1** (Marine Sonar Beacon: 🟢 Normal / 🔴 Alert)
3. **Card 3: Humidity (`humi`)** &rarr; Virtual Pin **V2** (Semicircular SVG Gauge: 0 to 100 %)
4. **Card 4: Water Level (`water`)** &rarr; Virtual Pin **V3** (Semicircular SVG Gauge: 0 to 500)

---

## Blynk Configuration (`java-backend/src/main/resources/application.properties`)

```properties
blynk.server=blynk.cloud
blynk.auth-token=YOUR_BLYNK_AUTH_TOKEN
blynk.template-id=TMPL3qzRJhyC8
blynk.template-name=sea water monitering
blynk.pins.temperature=v0
blynk.pins.waste=v1
blynk.pins.humidity=v2
blynk.pins.water=v3
```
*(You can also configure the Auth Token and server cluster through the web UI under **⚙️ Configuration**).*
