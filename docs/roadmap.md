# Karoo Forumslader Extension Roadmap

This document tracks the remaining high-priority improvements for the Karoo Forumslader Extension. All original Garmin `ref-companion` parity items (16 data fields, BLE device lock, companion settings, safety alerts, FIT recording, CSV/Wi-Fi log export, climb-aware range estimation, and V6 wireless DFU) are implemented.

See [GLOSSARY.md](../GLOSSARY.md) for canonical domain terminology.

---

## Recently Completed

- **Dynamic Battery Capacity & Health (`$FLC,5`)**: Parse BMS-measured full-charge capacity (`fullChargeCapacityMah`, `450..6000 mAh`) and `cycleCount`, persist last valid capacity per locked device, feed dynamic pack capacity (`Wh`) into `BatteryEstimator` (`ConsumerLoadModel` and `RouteEnergyForecaster`), and display pack capacity & cycles in the companion Dashboard.

---

## Upcoming Priorities

### 1. Idempotent USB/Consumer Control & 12V Light Detection Reconciliation
- Parse `FL_PROGCONSUMER` (`tokens[10]` in `$FL5`/`$FL6`) so the app knows whether consumer output is currently enabled.
- Make `SetConsumerState(enabled)` idempotent instead of blindly sending toggle command `$FLT,2`.
- Reconcile the `consumerCurrent` modeling contradiction between `ConsumerLoadModel` (12V battery bus draw) and `LightLoadDetector` (5V USB rail heuristic).

### 2. Power-Safe BLE Lifecycle & BLE Diagnostics
- **Power-Safe BLE Loop**: Automatically pause BLE scanning and reconnect loops when the ride is idle/paused or the Karoo suspends, conserving both Karoo and Forumslader battery.
- **BLE Diagnostics**: Track connection stability metrics (disconnects/hour, checksum data quality, disconnect count) and surface them in the companion Diagnostics view (and optionally as a diagnostic data field).

### 3. Telemetry & Codebase Hygiene
- Disable `DEBUG_SENTENCE_PARSING = true` / 1 Hz `Log.i` spam in `ForumsladerParser`.
- Align `PreferencesConstants.DEFAULT_HIGH_TEMP_THRESHOLD` (`40°C`) with `ForumsladerKarooAdapter` / `README.md` (`50°C`).
- Clarify or deduplicate `tripDistanceMeters` vs. `odometerMeters` in `ForumsladerParser`.
- Fix the `README.md` DFU command typo (`$FLT,6` → `$FLT,99`).
- Decompose God objects (`ForumsladerKarooAdapter`, `MainScreen`) and align `model/` & `extension/` packages with the 4-ring onion architecture.
