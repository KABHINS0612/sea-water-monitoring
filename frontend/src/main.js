/**
 * SEA WATER MONITORING SYSTEM - EXECUTIVE LUXURY JAVASCRIPT
 * Real-time polling from Spring Boot REST Backend (/api/sensors)
 * Luxury Semicircular Gauges & Marine Sonar Beacon Indicator
 */

const GAUGE_CIRCUMFERENCE = 251.327;

const state = {
    autoRefreshIntervalMs: 3000,
    countdownSeconds: 3,
    countdownTimer: null,
    isFetching: false,
    lastUpdatedTime: null,
    gaugeRanges: {
        temperature: { min: 0, max: 200 },
        humidity: { min: 0, max: 100 },
        water: { min: 0, max: 500 }
    }
};

const DOM = {
    statusPill: document.getElementById('statusPill'),
    statusDot: document.getElementById('statusDot'),
    statusText: document.getElementById('statusText'),
    lastUpdatedText: document.getElementById('lastUpdatedText'),
    countdownBadge: document.getElementById('countdownBadge'),
    refreshSelect: document.getElementById('refreshSelect'),
    btnRefresh: document.getElementById('btnRefresh'),
    refreshIcon: document.getElementById('refreshIcon'),
    displayServerName: document.getElementById('displayServerName'),
    globalAlert: document.getElementById('globalAlert'),
    alertMessage: document.getElementById('alertMessage'),
    btnCloseAlert: document.getElementById('btnCloseAlert'),

    // Card 1: Temperature
    valTemperature: document.getElementById('valTemperature'),
    fillGaugeTemp: document.getElementById('fillGaugeTemp'),
    minTempLabel: document.getElementById('minTempLabel'),
    maxTempLabel: document.getElementById('maxTempLabel'),
    tempStatusText: document.getElementById('tempStatusText'),
    pinTempBadge: document.getElementById('pinTempBadge'),

    // Card 2: Waste Detection (Marine Beacon)
    circularWasteLed: document.getElementById('circularWasteLed'),
    wasteStatusHeading: document.getElementById('wasteStatusHeading'),
    wasteStatusSub: document.getElementById('wasteStatusSub'),
    wasteStatusBadge: document.getElementById('wasteStatusBadge'),
    rawWasteValue: document.getElementById('rawWasteValue'),
    pinWasteBadge: document.getElementById('pinWasteBadge'),

    // Card 3: Humidity
    valHumidity: document.getElementById('valHumidity'),
    fillGaugeHumi: document.getElementById('fillGaugeHumi'),
    minHumiLabel: document.getElementById('minHumiLabel'),
    maxHumiLabel: document.getElementById('maxHumiLabel'),
    humiStatusText: document.getElementById('humiStatusText'),
    pinHumiBadge: document.getElementById('pinHumiBadge'),

    // Card 4: Water Level
    valWater: document.getElementById('valWater'),
    fillGaugeWater: document.getElementById('fillGaugeWater'),
    minWaterLabel: document.getElementById('minWaterLabel'),
    maxWaterLabel: document.getElementById('maxWaterLabel'),
    waterStatusText: document.getElementById('waterStatusText'),
    pinWaterBadge: document.getElementById('pinWaterBadge'),

    // Modal
    btnOpenSettings: document.getElementById('btnOpenSettings'),
    inputToken: document.getElementById('inputToken'),
    btnToggleToken: document.getElementById('btnToggleToken'),
    selectServer: document.getElementById('selectServer'),
    inputPinTemp: document.getElementById('inputPinTemp'),
    inputPinWaste: document.getElementById('inputPinWaste'),
    inputPinHumi: document.getElementById('inputPinHumi'),
    inputPinWater: document.getElementById('inputPinWater'),
    modalAlert: document.getElementById('modalAlert'),
    btnTestModal: document.getElementById('btnTestModal'),
    btnSaveModal: document.getElementById('btnSaveModal')
};

let settingsModalInstance = null;

function init() {
    if (window.bootstrap) {
        settingsModalInstance = new window.bootstrap.Modal(document.getElementById('settingsModal'));
    }
    bindEvents();
    loadConfigMetadata();
    fetchSensorData();
    startAutoRefresh();
}

async function fetchSensorData() {
    if (state.isFetching) return;
    state.isFetching = true;
    setSpinning(true);

    try {
        const response = await fetch('/api/sensors', { cache: 'no-store' });
        if (!response.ok) {
            throw new Error(`HTTP ${response.status} from backend`);
        }

        const data = await response.json();
        updateDashboard(data);
        hideAlert();
    } catch (error) {
        console.error('Failed to fetch sensor data:', error);
        handleConnectionError(error.message);
    } finally {
        state.isFetching = false;
        setSpinning(false);
        resetCountdown();
    }
}

function updateDashboard(data) {
    const now = new Date();
    state.lastUpdatedTime = now.toLocaleTimeString();
    DOM.lastUpdatedText.textContent = state.lastUpdatedTime;

    const isOnline = data.blynkStatus === 'ONLINE' || data.hardwareConnected === true;
    updateConnectionStatusUI(isOnline);

    if (data.pins) {
        if (data.pins.temperature) DOM.pinTempBadge.textContent = data.pins.temperature.toUpperCase();
        if (data.pins.waste) DOM.pinWasteBadge.textContent = data.pins.waste.toUpperCase();
        if (data.pins.humidity) DOM.pinHumiBadge.textContent = data.pins.humidity.toUpperCase();
        if (data.pins.water) DOM.pinWaterBadge.textContent = data.pins.water.toUpperCase();
    }

    if (data.gaugeRanges) {
        if (data.gaugeRanges.temperature) {
            state.gaugeRanges.temperature = data.gaugeRanges.temperature;
            DOM.minTempLabel.textContent = data.gaugeRanges.temperature.min;
            DOM.maxTempLabel.textContent = data.gaugeRanges.temperature.max;
        }
        if (data.gaugeRanges.humidity) {
            state.gaugeRanges.humidity = data.gaugeRanges.humidity;
            DOM.minHumiLabel.textContent = data.gaugeRanges.humidity.min;
            DOM.maxHumiLabel.textContent = data.gaugeRanges.humidity.max;
        }
        if (data.gaugeRanges.water) {
            state.gaugeRanges.water = data.gaugeRanges.water;
            DOM.minWaterLabel.textContent = data.gaugeRanges.water.min;
            DOM.maxWaterLabel.textContent = data.gaugeRanges.water.max;
        }
    }

    // =========================================================================
    // CARD 1: TEMPERATURE (temp)
    // =========================================================================
    if (data.temperature !== null && data.temperature !== undefined) {
        const tempVal = Number(data.temperature);
        DOM.valTemperature.textContent = tempVal.toFixed(1);
        setSemicircularGaugeFill(DOM.fillGaugeTemp, tempVal, state.gaugeRanges.temperature.min, state.gaugeRanges.temperature.max);
        
        if (tempVal < 15) {
            DOM.tempStatusText.textContent = 'Cold Ambient Sea Water (< 15°C)';
        } else if (tempVal <= 32) {
            DOM.tempStatusText.textContent = 'Pristine Marine Temperature (15–32°C)';
        } else {
            DOM.tempStatusText.textContent = 'Thermal Anomaly Detected (> 32°C)';
        }
    } else {
        DOM.valTemperature.textContent = '--';
        setSemicircularGaugeFill(DOM.fillGaugeTemp, 0, 0, 100);
        DOM.tempStatusText.textContent = 'Telemetry Pending (Check Pin V0)';
    }

    // =========================================================================
    // CARD 2: WASTE DETECTION (waste) - Marine Beacon
    // =========================================================================
    DOM.rawWasteValue.textContent = data.wasteRawValue !== null && data.wasteRawValue !== undefined
        ? String(data.wasteRawValue) : '--';

    if (data.wasteDetected !== null && data.wasteDetected !== undefined) {
        if (data.wasteDetected === true) {
            // ALERT: Floating Waste Detected -> Pulsing Laser Ruby
            DOM.circularWasteLed.className = 'beacon-lens-bezel beacon-alert';
            DOM.wasteStatusHeading.textContent = 'WASTE DETECTED';
            DOM.wasteStatusHeading.className = 'h5 fw-bold mb-1 text-danger';
            DOM.wasteStatusSub.textContent = 'Surface debris detected by optical sensor';
            DOM.wasteStatusBadge.className = 'waste-status-badge-lux badge bg-danger';
            DOM.wasteStatusBadge.textContent = 'ALERT';
        } else {
            // NORMAL: Clear Water -> Marine Emerald Beacon
            DOM.circularWasteLed.className = 'beacon-lens-bezel beacon-normal';
            DOM.wasteStatusHeading.textContent = 'NO WASTE DETECTED';
            DOM.wasteStatusHeading.className = 'h5 fw-bold mb-1 text-success';
            DOM.wasteStatusSub.textContent = 'Water surface is clear & pristine';
            DOM.wasteStatusBadge.className = 'waste-status-badge-lux badge bg-success';
            DOM.wasteStatusBadge.textContent = 'NORMAL';
        }
    } else {
        // Standby State
        DOM.circularWasteLed.className = 'beacon-lens-bezel beacon-neutral';
        DOM.wasteStatusHeading.textContent = 'AWAITING TELEMETRY';
        DOM.wasteStatusHeading.className = 'h5 fw-bold mb-1 text-muted';
        DOM.wasteStatusSub.textContent = 'Sensor disconnected or offline';
        DOM.wasteStatusBadge.className = 'waste-status-badge-lux badge bg-secondary';
        DOM.wasteStatusBadge.textContent = 'OFFLINE';
    }

    // =========================================================================
    // CARD 3: HUMIDITY (humi)
    // =========================================================================
    if (data.humidity !== null && data.humidity !== undefined) {
        const humiVal = Number(data.humidity);
        DOM.valHumidity.textContent = Math.round(humiVal);
        setSemicircularGaugeFill(DOM.fillGaugeHumi, humiVal, state.gaugeRanges.humidity.min, state.gaugeRanges.humidity.max);
        
        if (humiVal < 30) {
            DOM.humiStatusText.textContent = 'Arid Maritime Atmosphere (< 30%)';
        } else if (humiVal <= 70) {
            DOM.humiStatusText.textContent = 'Optimal Oceanic Humidity (30–70%)';
        } else {
            DOM.humiStatusText.textContent = 'Saturated Relative Humidity (> 70%)';
        }
    } else {
        DOM.valHumidity.textContent = '--';
        setSemicircularGaugeFill(DOM.fillGaugeHumi, 0, 0, 100);
        DOM.humiStatusText.textContent = 'Telemetry Pending (Check Pin V2)';
    }

    // =========================================================================
    // CARD 4: WATER LEVEL (water)
    // =========================================================================
    if (data.water !== null && data.water !== undefined) {
        const waterVal = Number(data.water);
        DOM.valWater.textContent = Math.round(waterVal);
        setSemicircularGaugeFill(DOM.fillGaugeWater, waterVal, state.gaugeRanges.water.min, state.gaugeRanges.water.max);
        
        const ratio = waterVal / state.gaugeRanges.water.max;
        if (ratio < 0.25) {
            DOM.waterStatusText.textContent = 'Low Reservoir / Tide Level';
        } else if (ratio <= 0.75) {
            DOM.waterStatusText.textContent = 'Nominal Water Level Range';
        } else {
            DOM.waterStatusText.textContent = 'High Water Mark Exceeded';
        }
    } else {
        DOM.valWater.textContent = '--';
        setSemicircularGaugeFill(DOM.fillGaugeWater, 0, 0, 500);
        DOM.waterStatusText.textContent = 'Telemetry Pending (Check Pin V3)';
    }

    if (data.errorMessage && !data.dataAvailable) {
        showAlert(`Notice: ${data.errorMessage}`);
    }
}

function setSemicircularGaugeFill(svgPathElement, value, min, max) {
    if (!svgPathElement) return;
    const clampedVal = Math.min(Math.max(value, min), max);
    const span = max - min;
    const ratio = span > 0 ? (clampedVal - min) / span : 0;
    const offset = GAUGE_CIRCUMFERENCE * (1 - ratio);
    svgPathElement.style.strokeDashoffset = offset.toFixed(2);
}

function updateConnectionStatusUI(isOnline) {
    if (isOnline) {
        DOM.statusPill.className = 'status-pill-luxury status-pill-online';
        DOM.statusDot.className = 'status-dot-gem dot-online';
        DOM.statusText.textContent = 'ONLINE';
    } else {
        DOM.statusPill.className = 'status-pill-luxury status-pill-offline';
        DOM.statusDot.className = 'status-dot-gem dot-offline';
        DOM.statusText.textContent = 'OFFLINE';
    }
}

function handleConnectionError(msg) {
    updateConnectionStatusUI(false);
    DOM.valTemperature.textContent = 'N/A';
    DOM.valHumidity.textContent = 'N/A';
    DOM.valWater.textContent = 'N/A';
    DOM.circularWasteLed.className = 'beacon-lens-bezel beacon-neutral';
    DOM.wasteStatusHeading.textContent = 'BACKEND OFFLINE';
    DOM.wasteStatusHeading.className = 'h5 fw-bold mb-1 text-muted';
    DOM.wasteStatusBadge.className = 'waste-status-badge-lux badge bg-secondary';
    DOM.wasteStatusBadge.textContent = 'OFFLINE';
    showAlert(`Telemetry Gateway Error: ${msg}. Confirm that Spring Boot backend is active on port 8080.`);
}

function startAutoRefresh() {
    stopAutoRefresh();
    if (state.autoRefreshIntervalMs <= 0) {
        DOM.countdownBadge.textContent = 'manual';
        return;
    }

    state.countdownSeconds = Math.round(state.autoRefreshIntervalMs / 1000);
    DOM.countdownBadge.textContent = `in ${state.countdownSeconds}s`;

    state.countdownTimer = setInterval(() => {
        state.countdownSeconds--;
        if (state.countdownSeconds <= 0) {
            state.countdownSeconds = Math.round(state.autoRefreshIntervalMs / 1000);
            fetchSensorData();
        }
        DOM.countdownBadge.textContent = `in ${state.countdownSeconds}s`;
    }, 1000);
}

function stopAutoRefresh() {
    if (state.countdownTimer) {
        clearInterval(state.countdownTimer);
        state.countdownTimer = null;
    }
}

function resetCountdown() {
    if (state.autoRefreshIntervalMs > 0) {
        state.countdownSeconds = Math.round(state.autoRefreshIntervalMs / 1000);
        DOM.countdownBadge.textContent = `in ${state.countdownSeconds}s`;
    }
}

function setSpinning(isSpinning) {
    if (isSpinning) {
        DOM.refreshIcon.classList.add('rotating');
        DOM.btnRefresh.disabled = true;
    } else {
        DOM.refreshIcon.classList.remove('rotating');
        DOM.btnRefresh.disabled = false;
    }
}

function showAlert(message) {
    DOM.alertMessage.textContent = message;
    DOM.globalAlert.style.display = 'block';
}

function hideAlert() {
    DOM.globalAlert.style.display = 'none';
}

async function loadConfigMetadata() {
    try {
        const res = await fetch('/api/blynk/config');
        if (res.ok) {
            const conf = await res.json();
            if (conf.server) {
                DOM.displayServerName.textContent = conf.server;
                DOM.selectServer.value = conf.server;
            }
            if (conf.pins) {
                if (conf.pins.temperature) DOM.inputPinTemp.value = conf.pins.temperature;
                if (conf.pins.waste) DOM.inputPinWaste.value = conf.pins.waste;
                if (conf.pins.humidity) DOM.inputPinHumi.value = conf.pins.humidity;
                if (conf.pins.water) DOM.inputPinWater.value = conf.pins.water;
            }
            if (conf.tokenConfigured === false) {
                showAlert('Blynk Auth Token is not configured. Click the Settings icon to configure your Auth Token.');
            }
        }
    } catch (e) {
        console.debug('Config metadata notice:', e);
    }
}

function bindEvents() {
    DOM.btnRefresh.addEventListener('click', fetchSensorData);

    DOM.refreshSelect.addEventListener('change', (e) => {
        state.autoRefreshIntervalMs = parseInt(e.target.value, 10);
        startAutoRefresh();
    });

    DOM.btnCloseAlert.addEventListener('click', hideAlert);

    DOM.btnOpenSettings.addEventListener('click', () => {
        DOM.modalAlert.style.display = 'none';
        if (settingsModalInstance) settingsModalInstance.show();
    });

    DOM.btnToggleToken.addEventListener('click', () => {
        const isPassword = DOM.inputToken.type === 'password';
        DOM.inputToken.type = isPassword ? 'text' : 'password';
        document.getElementById('eyeIcon').textContent = isPassword ? '🙈' : '👁️';
    });

    DOM.btnTestModal.addEventListener('click', async () => {
        DOM.btnTestModal.disabled = true;
        DOM.btnTestModal.textContent = 'Testing...';
        DOM.modalAlert.className = 'alert alert-info py-2 small mb-0 rounded-3';
        DOM.modalAlert.textContent = 'Connecting to Blynk IoT Cloud...';
        DOM.modalAlert.style.display = 'block';

        try {
            const res = await fetch('/api/blynk/status');
            const status = await res.json();
            if (status.online) {
                DOM.modalAlert.className = 'alert alert-success py-2 small mb-0 rounded-3';
                DOM.modalAlert.textContent = `Connection Successful! Hardware Node ONLINE (Latency: ${status.latencyMs} ms)`;
            } else if (status.cloudReachable) {
                DOM.modalAlert.className = 'alert alert-warning py-2 small mb-0 rounded-3';
                DOM.modalAlert.textContent = `Blynk Cloud reachable, but hardware node is OFFLINE (${status.message})`;
            } else {
                DOM.modalAlert.className = 'alert alert-danger py-2 small mb-0 rounded-3';
                DOM.modalAlert.textContent = `Connection Failed: ${status.message}`;
            }
        } catch (err) {
            DOM.modalAlert.className = 'alert alert-danger py-2 small mb-0 rounded-3';
            DOM.modalAlert.textContent = `Test failed: ${err.message}`;
        } finally {
            DOM.btnTestModal.disabled = false;
            DOM.btnTestModal.textContent = 'Test Connection';
        }
    });

    DOM.btnSaveModal.addEventListener('click', async () => {
        const token = DOM.inputToken.value.trim();
        const server = DOM.selectServer.value;
        const pinTemp = DOM.inputPinTemp.value.trim();
        const pinWaste = DOM.inputPinWaste.value.trim();
        const pinHumi = DOM.inputPinHumi.value.trim();
        const pinWater = DOM.inputPinWater.value.trim();

        DOM.btnSaveModal.disabled = true;
        DOM.btnSaveModal.textContent = 'Saving...';

        try {
            const res = await fetch('/api/blynk/config', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    authToken: token,
                    server,
                    pinTemperature: pinTemp,
                    pinWaste: pinWaste,
                    pinHumidity: pinHumi,
                    pinWater: pinWater
                })
            });

            if (!res.ok) throw new Error(`HTTP ${res.status}`);

            DOM.modalAlert.className = 'alert alert-success py-2 small mb-0 rounded-3';
            DOM.modalAlert.textContent = 'Configuration saved! Reconnecting to Blynk Cloud...';
            setTimeout(() => {
                if (settingsModalInstance) settingsModalInstance.hide();
                fetchSensorData();
                startAutoRefresh();
            }, 800);
        } catch (e) {
            DOM.modalAlert.className = 'alert alert-danger py-2 small mb-0 rounded-3';
            DOM.modalAlert.textContent = `Failed to save: ${e.message}`;
        } finally {
            DOM.btnSaveModal.disabled = false;
            DOM.btnSaveModal.textContent = 'Save & Connect';
        }
    });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
} else {
    init();
}
