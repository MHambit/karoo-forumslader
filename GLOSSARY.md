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
