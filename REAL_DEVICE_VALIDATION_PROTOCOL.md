# PrivacyView • Real Device Validation Protocol

This document outlines the testing and validation procedure to evaluate PrivacyView on physical Android hardware across different display technologies (OLED, AMOLED, LTPO, IPS LCD) and manufacturer OEM skins (Google Pixel, Samsung One UI, Xiaomi HyperOS, OnePlus OxygenOS).

---

## 1. Test Equipment & Environment Setup

### Required Hardware:
1. **Primary Test Device**: Modern Android smartphone running Android 10 to Android 15.
2. **Display Variants**:
   - OLED / AMOLED device (e.g., Google Pixel 7/8/9, Samsung Galaxy S22/S23/S24).
   - IPS LCD device (e.g., Moto G series, Poco/Redmi entry/mid-tier).
3. **Ambient Lighting Conditions**:
   - **Indoor Office / Train / Cafe Lighting**: 300 to 500 lux.
   - **Dim / Low-Light Setting**: <50 lux.
   - **Bright Daylight / Glare**: >2,000 lux.

---

## 2. Test Cases & Validation Metrics

### Test 1: Direct Line-of-Sight Readability (0° Perpendicular)
* **Goal**: Confirm that the user directly operating the phone can read comfortably without eye strain.
* **Procedure**:
  1. Open a confidential text screen (e.g., Banking App, Email, or Messaging).
  2. Turn PrivacyView **ON**.
  3. Test **Dark Tint** at 30%, 55%, and 75% strength.
  4. Test **Blur / Camouflage** mode.
* **Pass Criteria**:
  - Direct user (holding phone 25–35 cm away) must comfortably read 14pt body text.
  - No flickering or noticeable framerate drops (maintain 60Hz/120Hz scrolling smoothness).

---

### Test 2: Side-Angle Shoulder Surfing Mitigation (45° and 60° Angles)
* **Goal**: Measure visual attenuation and legibility degradation for a bystander sitting beside the user.
* **Procedure**:
  1. Position a test observer 0.8 to 1.2 meters away at a 45° angle, then a 60° angle.
  2. Display a random 6-digit 2FA code and account balance on the screen.
  3. Observer attempts to read the code with PrivacyView **OFF** vs **ON** (at 65% Dark Tint).
* **Physical Reality Observation**:
  - Notice how the natural 50%–70% OLED off-axis luminance falloff compounds with the dark overlay.
  - Ambient room light reflecting off the outer Gorilla Glass creates surface glare that overwhelms the attenuated subpixel emission, obscuring the digits from the side observer.

---

### Test 3: System-Wide Touch & Tapjacking Safety Validation
* **Goal**: Verify that normal touches pass through seamlessly, while observing Android's security boundaries.
* **Procedure**:
  1. Turn PrivacyView **ON**.
  2. Navigate through third-party apps (Chrome, Instagram, Maps, YouTube). Confirm touch responsiveness is 100% identical to normal usage (0ms lag).
  3. Trigger an Android system permission prompt (e.g., "Allow app to access notifications?").
* **Expected Android Behavior**:
  - Android 12+ includes clickjacking/tapjacking mitigation (`FLAG_WINDOW_IS_OBSCURED`).
  - On certain OEM devices, tapping "Allow" on a high-privilege system dialog while an overlay is active will trigger a toast: *"An app is obscuring a permission request."*
  - **Remedy**: Tapping the Quick Settings tile immediately dismisses PrivacyView, allowing the user to grant the permission and re-enable it in 1 second.

---

### Test 4: Quick Settings Tile & Widget Response Time
* **Procedure**:
  1. Add the PrivacyView tile to the Quick Settings notification shade.
  2. Add the PrivacyView widget to the home screen.
  3. Tap the tile/widget from other apps without opening the PrivacyView main activity.
* **Pass Criteria**:
  - Transition must occur within <100ms.
  - Status indicator must remain synchronized between tile, widget, and persistent notification.

---

### Test 5: Battery, CPU, and Memory Profiling
* **Procedure**:
  1. Connect device to Android Studio Profiler via ADB (`adb shell dumpsys cpuinfo`).
  2. Leave PrivacyView active for 30 minutes with static content.
* **Pass Criteria**:
  - CPU usage: **0.0%** (no redundant invalidation loops or animation tickers running when idle).
  - RAM consumption: **<35 MB**.
