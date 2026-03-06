# Autonomous Mode Benchmark Analysis
## Team 3164 - Score & Collect Mode (Mode 1)

### Executive Summary

Ran 500 simulations of the "Score & Collect" autonomous routine. The robot successfully scores all 3 preloaded FUEL with 100% consistency but fails to collect any additional FUEL during the 20-second AUTO period.

---

## Results Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    FUEL SCORING RESULTS                          │
├─────────────────────────────────────────────────────────────────┤
│  Average FUEL Scored:  3.00                                      │
│  Standard Deviation:   0.00                                      │
│  Min/Max:              3 / 3                                     │
│  Success Rate:         100% (500/500 matches)                    │
└─────────────────────────────────────────────────────────────────┘
```

### Timing Breakdown (20 Second AUTO Period)

```
Phase Distribution:
═══════════════════════════════════════════════════════════════════

SHOOTING    ▓░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  0.6s  (3%)
DRIVING     ▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  4.0s  (20%)
INTAKE      ▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  5.0s  (25%)
COMPLETE    ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░ 10.4s  (52%)

═══════════════════════════════════════════════════════════════════
```

### Shot Timing Analysis

```
Timeline (seconds):
0         5         10        15        20
├─────────┼─────────┼─────────┼─────────┤
▼▼▼
↑↑↑ All 3 shots fired between 0.72s - 0.76s
    │
    └──── EXTREMELY EFFICIENT! (0.04s total shooting time)
```

**Key Observations:**
- First Shot: 0.72 seconds
- Last Shot: 0.76 seconds
- Total Shooting Time: 0.04 seconds for 3 FUEL

---

## Robot Movement Analysis

```
Field Layout (not to scale):
═════════════════════════════════════════════════════════════════════

       Blue Alliance Zone        │  Neutral Zone  │   Red Alliance Zone
      (0m - 4.03m)              │ (4.03m-12.51m) │   (12.51m - 16.54m)
                                │                │
  ┌────┐                        │     FUEL       │
  │BLUE│                        │    ○ ○ ○       │
  │ HUB│←── Shooting Position   │   ○  ○  ○     │
  └────┘    (2.5m)              │    ○ ○ ○       │
                                │                │
START                   END     │   TARGET       │
  ●─────────────────────→○      │     ◎          │
 1.5m                  2.95m    │   8.27m        │
                                │                │
═════════════════════════════════════════════════════════════════════

Distance traveled: 4.7m
Distance to neutral zone: 5.3m (NEVER REACHED!)
```

---

## Root Cause Analysis

### Problem: Robot Never Reaches FUEL Collection Zone

| Issue | Current Value | Impact |
|-------|--------------|--------|
| Starting Position | x = 1.5m | Good, close to hub |
| Ending Position | x = 2.95m | Only 1.45m forward progress |
| Neutral Zone | x = 8.27m | 5.3m short of target |
| DRIVE_TO_NEUTRAL_TIME | 4.0 seconds | Timer expires before arrival |
| INTAKE_TIMEOUT | 5.0 seconds | Wasted - no FUEL present |

### Sequence of Events:

1. **0.00 - 0.72s**: Shooter spins up, robot aims at hub
2. **0.72 - 0.76s**: All 3 FUEL fired (SUCCESS)
3. **0.76 - 4.76s**: Driving toward neutral zone (4.0s timer)
4. **4.76 - 9.76s**: Intake phase (5.0s) - BUT NO FUEL NEARBY
5. **9.76 - 20.0s**: Complete phase - robot idle

---

## Performance Improvement Recommendations

### 1. **Parallel Movement During Shooting** (HIGH IMPACT)
```
Current:      SHOOT ──→ DRIVE ──→ INTAKE
Recommended:  SHOOT + DRIVE ──→ INTAKE

Expected gain: +4 seconds of driving time
```

**Implementation:**
```java
// In SCORING_PRELOAD phase, drive toward target while shooting
if (state.fuelCount > 0) {
    input.spinUp = true;
    // NEW: Move toward neutral zone while shooting
    driveToTarget(state, input, Constants.Field.CENTER_X, Constants.Field.CENTER_Y);
    boolean readyToShoot = aimAtHub(state, input);
    if (readyToShoot) {
        input.shoot = true;
    }
}
```

### 2. **Increase Drive Speed** (MEDIUM IMPACT)
```
Current drive speed:  0.8 (capped in driveToTarget)
Distance to cover:    6.77m (from 1.5m to 8.27m)
Current time:         ~8.5 seconds at 0.8 m/s effective speed

Recommended:          1.0 (maximum safe speed)
New time:             ~6.8 seconds
```

### 3. **Target Closer FUEL Sources** (HIGH IMPACT)

Instead of targeting center field (8.27m), target FUEL closer to alliance zone:
- Blue Depot FUEL: x = ~1.0m (already behind starting position)
- Offset Neutral FUEL: x = ~6.27m (2m closer than center)

```
New target: x = 6.0m instead of x = 8.27m
Distance saved: 2.27m
Time saved: ~2.8 seconds
```

### 4. **Preload Additional FUEL at Closer Position** (LOW IMPACT)

Consider autonomous routines that score preload then collect from alliance depot first (mode 4: Depot Raid).

### 5. **Reduce Timeout Values** (LOW IMPACT)

```
INTAKE_TIMEOUT: 5.0s → 3.0s (if no FUEL detected)
This frees up 2 seconds for a second scoring attempt
```

---

## Expected Results After Improvements

| Metric | Current | After Optimization | Improvement |
|--------|---------|-------------------|-------------|
| FUEL Scored | 3.00 | 5-6 (estimated) | +67-100% |
| Time to First FUEL Collection | N/A | ~5.0s | NEW |
| Neutral Zone Arrival | Never | ~6.0s | Achievable |
| End Position X | 2.95m | ~7.0m | +4.05m |

---

## ASCII Performance Graph

```
FUEL Scored per Match (500 simulations)
8 ┤
7 ┤
6 ┤
5 ┤
4 ┤
3 ┤████████████████████████████████████████ ← Current (100% at 3)
2 ┤
1 ┤
0 ┼────────────────────────────────────────
   Match 1                              Match 500

Target after optimization: 5-6 FUEL
```

---

## OPTIMIZATION RESULTS (IMPLEMENTED)

### Summary
After implementing the optimizations, the autonomous routine now scores **7 FUEL** instead of 3 - a **133% improvement**!

### Before vs After Comparison

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| **FUEL Scored** | 3.00 | 7.00 | **+133%** |
| Distance Traveled | 5.9 m | 19.4 m | +229% |
| Time in Shooting | 0.6 s (3%) | 2.2 s (11%) | +267% |
| Time in Driving | 8.0 s (40%) | 9.8 s (49%) | +23% |
| First Shot Time | 0.72 s | 0.72 s | Same |
| Last Shot Time | 0.76 s | 13.02 s | +12.26 s |
| End Phase | COMPLETE | COMPLETE | Same |

### Optimizations Applied

1. **Hub Navigation** - Robot now paths around the HUB instead of colliding with it
   - Added hub avoidance in DRIVING_TO_NEUTRAL phase
   - Added hub avoidance in POSITIONING_TO_SHOOT phase

2. **Increased Drive Speed** - From 0.8 to 1.0 (max safe speed)

3. **Extended Drive Timeout** - From 4.0s to 8.0s to allow robot to reach neutral zone

4. **Reduced Intake Timeout** - From 5.0s to 4.0s for faster cycle times

5. **Closer FUEL Target** - Target x = CENTER_X - 2.0 (closer to alliance zone)

6. **Faster Intake Speed** - Increased from 0.2 to 0.4 during intake phase

### Sequence Comparison

**Before (3 FUEL):**
```
0.00s  ─── Start AUTO
0.72s  ─── Shoot 3 preload FUEL
0.76s  ─── Begin driving to neutral zone
4.76s  ─── HIT HUB COLLISION - stuck at x=2.95
9.76s  ─── Intake phase ends (no FUEL found)
20.0s  ─── AUTO ends
```

**After (7 FUEL):**
```
0.00s  ─── Start AUTO
0.72s  ─── Shoot 3 preload FUEL
0.76s  ─── Begin driving AROUND hub
~5.0s  ─── Reach neutral zone, collect FUEL
~6.0s  ─── Collected 4 FUEL, return to alliance zone
~10.0s ─── Navigate around hub, reach shooting position
~13.0s ─── Shoot 4 collected FUEL
20.0s  ─── AUTO ends (COMPLETE phase)
```

### Performance Metrics

```
BEFORE OPTIMIZATION:
════════════════════════════════════════════════════
FUEL   ▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░░░░░  3 FUEL
════════════════════════════════════════════════════

AFTER OPTIMIZATION:
════════════════════════════════════════════════════
FUEL   ▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░  7 FUEL
════════════════════════════════════════════════════
              ↑ +133% improvement!
```

---

## Files Modified

1. **AutonomousController.java**
   - Added hub avoidance in `DRIVING_TO_NEUTRAL` phase
   - Added hub avoidance in `POSITIONING_TO_SHOOT` phase
   - Updated timing constants (DRIVE_TO_NEUTRAL_TIME, INTAKE_TIMEOUT)
   - Updated `driveToTarget()` to use max speed 1.0
   - Increased intake forward speed to 0.4

2. **AutoModeBenchmark.java**
   - Fixed score tracking (use matchState.blueFuelScored)
   - Added timing breakdown analysis

---

*Generated by AutoModeBenchmark - Team 3164 Simulator*
*500 simulations completed in 0.4 seconds (1,385 matches/sec)*
*Optimization complete: 3 → 7 FUEL (+133%)*
