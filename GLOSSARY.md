# Karoo Forumslader

Connects a Forumslader bicycle dynamo charger to a Hammerhead Karoo cycling computer for real-time power telemetry, climb-aware battery range estimation, safety alerts, and device management.

## Language

### Battery & Capacity

**State of Charge (SoC)**:
Remaining battery charge expressed as a percentage (0–100%).
_Avoid_: battery level, remaining capacity %

**Full-Charge Capacity**:
The BMS-measured actual total energy capacity of the battery pack (in mAh or Wh), reflecting cell size and aging.
_Avoid_: nominal capacity, max battery, battery size

**Default Pack Capacity**:
The fallback full-charge capacity (13.3 Wh, representing a standard 3S 1.2 Ah Li-Ion pack at 11.1 V nominal) assumed when no valid BMS measurement is available.
_Avoid_: fixed capacity, hardcoded capacity

### BLE Diagnostics

**Unexpected Disconnect**:
An unintended loss of the active BLE link while connected to a Forumslader, excluding intentional teardowns (user disconnect, device unlock/switch, DFU reboot, or extension shutdown).
_Avoid_: disconnect, drop, reconnect

**Monitoring Uptime**:
Elapsed wall-clock time since the first successful BLE connection in the current diagnostic session (resets on extension restart, locked-device change, or manual reset).
_Avoid_: connected time, session length

**Disconnect Rate**:
Unexpected disconnects per hour, computed over at least one hour (`unexpectedDisconnects / max(1.0, uptimeHours)`) to prevent early-session spikes.
_Avoid_: drop rate, reconnect rate

**Stability Index**:
BLE link reliability percentage derived from the disconnect rate: `max(0%, (1 - disconnectRate) * 100%)`.
_Avoid_: connection score, BLE health

**Data Quality**:
Percentage of checksummed NMEA sentences that passed XOR checksum validation (`pass / (pass + fail) * 100%`). Malformed non-hex checksums count as failures; unchecksummed sentences are excluded from the denominator.
_Avoid_: checksum rate, packet integrity
