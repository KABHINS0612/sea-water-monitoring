# 🌊 Sea Water Monitoring System

> An **Executive IoT Dashboard** for real-time sea water quality monitoring. Built with a **Java 17 Spring Boot REST API Backend**, a **Node.js Luxury Marine Frontend**, and connected directly to **Blynk IoT Cloud**.

---

## ⚡ Quick Start: How to Run

### 1️⃣ Start Java Backend (Terminal 1)

```bash
cd java-backend
mvn spring-boot:run
```
* **Backend API:** [http://localhost:8080/api/sensors](http://localhost:8080/api/sensors)

---

### 2️⃣ Start Frontend (Terminal 2)

```bash
cd frontend
npm run dev
```
* **Dashboard Website:** [http://localhost:3000](http://localhost:3000)

*(Starts immediately with zero external dependencies required)*

---

## 🏗️ System Architecture

```
   ┌────────────────────────────────────────┐
   │     ESP32 / ESP8266 IoT Hardware       │
   │  Temperature • Waste • Humidity • Water│
   └───────────────────┬────────────────────┘
                       │ Wi-Fi (Blynk Protocol)
                       ▼
   ┌────────────────────────────────────────┐
   │            Blynk IoT Cloud             │
   │   Template: TMPL3qzRJhyC8 (blynk.cloud)│
   └───────────────────┬────────────────────┘
                       │ HTTPS REST API
                       ▼
   ┌────────────────────────────────────────┐
   │   Java 17 Spring Boot Backend (:8080)  │
   │   • Secure Token Storage               │
   │   • REST API Endpoints (/api/sensors)  │
   └───────────────────┬────────────────────┘
                       │ HTTP / Proxy
                       ▼
   ┌────────────────────────────────────────┐
   │     Node.js Luxury Frontend (:3000)    │
   │   • 3 Semicircular SVG Gauges          │
   │   • 1 Marine Sonar Beacon Indicator    │
   │   • 5 Full Executive Dashboard Pages   │
   └────────────────────────────────────────┘
```

---

## 📊 4 Core Monitoring Cards (Blynk Virtual Pins)

| # | Sensor Metric | Virtual Pin | Widget Type | Range / Display States |
| :---: | :--- | :---: | :--- | :--- |
| **1** | **Temperature** (`temp`) | **`V0`** | Semicircular SVG Gauge | **0 to 200 °C** (Ice Cyan &rarr; Sapphire &rarr; Coral) |
| **2** | **Waste Detection** (`waste`) | **`V1`** | Marine Sonar Beacon | **🟢 Normal (`NO WASTE DETECTED`)**<br>**🔴 Alert (`WASTE DETECTED`)** |
| **3** | **Humidity** (`humi`) | **`V2`** | Semicircular SVG Gauge | **0 to 100 %** (Marine Emerald &rarr; Aquamarine) |
| **4** | **Water Level** (`water`) | **`V3`** | Semicircular SVG Gauge | **0 to 500 Level** (Deep Pacific &rarr; Sky Azure) |

> **Note:** Gauge display ranges are visual boundaries and **do not** alter the actual sensor readings received from your hardware.

---

## 🧭 The 5 Dashboard Pages

You can switch between any of these pages using the top navigation bar:

| Page | URL Path | Description |
| :--- | :--- | :--- |
| **🌊 Live Monitor** | `http://localhost:3000/index.html` | Real-time live dashboard with the 4 core sensor cards, connection status pill, and auto-sync. |
| **📈 Analytics** | `http://localhost:3000/analytics.html` | Interactive time-series telemetry charts (Chart.js), KPI tiles (Peak Temp, Tide Level, Humidity), and CSV download. |
| **🚨 Incident Logs** | `http://localhost:3000/alerts.html` | Audit log of all environmental incidents, contamination events, severity badges (`CRITICAL`, `WARNING`, `INFO`). |
| **⚡ Hardware** | `http://localhost:3000/devices.html` | ESP32 microcontroller specs, 4-bar Wi-Fi RSSI signal meter, Virtual Pin mapping, and live terminal stream. |
| **⚙️ Configuration** | `http://localhost:3000/settings.html` | Manage your Blynk Auth Token, switch cloud clusters, and calibrate dial boundaries. |

---

## 🔑 Blynk Configuration

### Where to add your Auth Token:

Open `java-backend/src/main/resources/application.properties`:

```properties
# Blynk Cloud Server Cluster
blynk.server=blynk.cloud

# Paste your 32-character Blynk Device Auth Token here:
blynk.auth-token=YOUR_ACTUAL_BLYNK_AUTH_TOKEN

# Template Details
blynk.template-id=TMPL3qzRJhyC8
blynk.template-name=sea water monitering

# Virtual Pin Assignments
blynk.pins.temperature=v0
blynk.pins.waste=v1
blynk.pins.humidity=v2
blynk.pins.water=v3

# Configurable Gauge Ranges
blynk.gauge.temperature.min=0.0
blynk.gauge.temperature.max=200.0
blynk.gauge.humidity.min=0.0
blynk.gauge.humidity.max=100.0
blynk.gauge.water.min=0.0
blynk.gauge.water.max=500.0
```

> **Security Note:** The Auth Token is stored and executed exclusively on the Spring Boot backend server. It is **never** exposed in frontend JavaScript source code.

*(You can also update your token directly in the browser by visiting the **⚙️ Configuration** page).*

---

## 📂 Project Structure

```text
sea-water-monitoring/
│
├── java-backend/                          # Java 17 Spring Boot Backend
│   ├── pom.xml                            # Maven dependencies & build configuration
│   ├── src/main/java/com/seawater/monitoring/
│   │   ├── MainApplication.java          # Spring Boot main entry point
│   │   ├── config/BlynkConfig.java       # Properties binder & RestTemplate bean
│   │   ├── controller/SensorController.java # REST API endpoints (/api/sensors)
│   │   ├── model/BlynkStatus.java        # Hardware & connection status model
│   │   ├── model/SensorData.java         # 4-sensor data model
│   │   └── service/BlynkService.java     # Direct Blynk Cloud REST client
│   └── src/main/resources/
│       └── application.properties        # Server port, Blynk token & pin mappings
│
└── frontend/                              # Node.js Luxury Marine Frontend
    ├── package.json                       # npm run dev script
    ├── server.js                          # Built-in lightweight Node server with API proxy
    ├── index.html                         # 🌊 Live Monitor Dashboard
    ├── analytics.html                     # 📈 Analytics & Trends Page
    ├── alerts.html                        # 🚨 Incident Logs & Alarms Page
    ├── devices.html                       # ⚡ Hardware & Network Diagnostics Page
    ├── settings.html                      # ⚙️ Blynk Cloud Configuration Page
    └── src/
        ├── style.css                      # Luxury marine glassmorphism styling
        └── main.js                        # Real-time WebSocket/polling client
```

---

## 🌐 REST API Endpoints Reference

All endpoints return clean, formatted JSON data:

| Method | Endpoint | Description |
| :---: | :--- | :--- |
| `GET` | `/api/sensors` | Returns all 4 sensor readings, status, timestamps, and gauge ranges. |
| `GET` | `/api/sensors/temperature` | Live temperature value (`°C`) on Pin V0. |
| `GET` | `/api/sensors/waste` | Waste detection status (`WASTE DETECTED` / `NO WASTE DETECTED`). |
| `GET` | `/api/sensors/humidity` | Live humidity value (`%`) on Pin V2. |
| `GET` | `/api/sensors/water` | Live water level reading on Pin V3. |
| `GET` | `/api/blynk/status` | Blynk Cloud connection status (`ONLINE` / `OFFLINE`) and latency. |
| `GET` | `/api/blynk/config` | Reads current non-sensitive pin mappings and template info. |
| `POST` | `/api/blynk/config` | Updates Auth Token or pin mappings dynamically from the web UI. |

### Sample JSON Output (`GET /api/sensors`):
```json
{
  "temperature": 27.5,
  "wasteDetected": false,
  "wasteStatus": "NO WASTE DETECTED",
  "wasteRawValue": "0",
  "humidity": 64.0,
  "water": 142.0,
  "blynkStatus": "ONLINE",
  "hardwareConnected": true,
  "dataAvailable": true,
  "timestamp": "2026-11-14T01:00:00.000Z",
  "pins": {
    "temperature": "v0",
    "waste": "v1",
    "humidity": "v2",
    "water": "v3"
  },
  "gaugeRanges": {
    "temperature": { "min": 0.0, "max": 200.0 },
    "humidity": { "min": 0.0, "max": 100.0 },
    "water": { "min": 0.0, "max": 500.0 }
  }
}
```

---

## ❓ Frequently Asked Questions & Troubleshooting

#### 1. Hardware status shows "OFFLINE"
* Check if your ESP32 / ESP8266 is powered on and connected to your Wi-Fi network.
* Verify that your device shows as **Online** in the [Blynk Web Console](https://blynk.cloud).

#### 2. Values display as `--` or `N/A`
* Ensure your Arduino code writes to the correct pins:
  ```cpp
  Blynk.virtualWrite(V0, temperature);
  Blynk.virtualWrite(V1, wasteStatus);
  Blynk.virtualWrite(V2, humidity);
  Blynk.virtualWrite(V3, waterLevel);
  ```
* If your hardware uses different pins, adjust them in `application.properties` or on the **⚙️ Configuration** page.

#### 3. Frontend says "Backend Connection Error"
* Ensure your Java backend is running on port 8080 (`mvn spring-boot:run` inside `java-backend/`).
* The frontend proxies all `/api` requests to `http://localhost:8080`.
