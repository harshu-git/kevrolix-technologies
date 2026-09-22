# Anti Phone Snatcher - Google Play Store Listing & Submission Guide

This document contains everything needed to complete your Google Play Console submission for **Anti Phone Snatcher**.

---

## 1. Store Listing Details

### App Title (Max 30 characters)
`Anti Phone Snatcher`

### Short Description (Max 80 characters)
`Instant privacy blackout curtain and anti-snatch theft defense for your phone.`

### Full Description (Max 4000 characters)
```text
Protect your personal screen and shield your device against public shoulder surfers, thieves, and phone snatchers.

Anti Phone Snatcher is an ultra-fast, 100% on-device privacy shield engineered to protect your personal privacy in public spaces, trains, buses, cafes, and crowded streets.

🔥 KEY SECURITY & PRIVACY FEATURES:

🛡️ INSTANT PRIVACY CURTAIN
Activate a stealth blackout overlay or adjustable privacy blinds over your screen with a single tap, quick settings tile, or physical hardware button. Keep private chats, banking apps, and sensitive notes invisible to prying eyes next to you.

⚡ ANTI-SNATCH SENTINEL (MOTION-TRIGGERED DEFENSE)
Walking on busy streets or public transit? Arm Anti-Snatch mode. Anti Phone Snatcher utilizes real-time motion vector analytics to detect sudden aggressive snatch attempts or unexpected acceleration. The moment someone grabs your phone from your hand, Anti Phone Snatcher instantly locks the screen, mutes audio, and requires your secret PIN or knock sequence to disarm.

🔒 FORT KNOX ANTI-TAMPER SYSTEM
Thieves often attempt to pull down the notification shade or access Android settings to turn on airplane mode or disable security apps. Anti Phone Snatcher's Anti-Tamper Shield blocks notification panel pull-downs and immediately locks down the device if system settings are manipulated during blackout.

🔊 HARDWARE BUTTON EMERGENCY SHORTCUT
Double-press the Volume Down key to instantly blank the screen without waking the device or opening the app. Perfect for quick discreet concealment when someone approaches your desk or seat.

🔐 UNBREAKABLE RECOVERY METHODS
- Secret 4-digit master PIN.
- Custom Morse-like Tap Sequence (unlock by tapping anywhere on the black screen in your secret rhythm).

✨ ZERO DATA COLLECTION • 100% PRIVATE
- Completely offline: No internet permission required.
- No analytics, no tracking SDKs, no servers.
- Your data and phone activity remain 100% in your hands.
```

---

## 2. Google Play Console App Setup & Policies

### A. Monetization / Pricing
- Navigate to: **Monetize > Products > App pricing**.
- Set to **Paid**.
- Choose your target upfront price (e.g. $1.99 / $2.99 / ₹99 / €2.49).
- *Note: In-app purchases are disabled; users buy the full license directly upon Play Store installation.*

### B. App Content & Declarations

#### 1. Data Safety Questionnaire
- **Does your app collect or share any user data?** Select **NO**.
- **Is all user data encrypted in transit?** App does not collect or transmit data.
- **Does your app provide a way for users to request data deletion?** App stores zero user data.

#### 2. Target Audience & Content Rating
- **Target Age:** Select **18 and over** (or 13+).
- **Questionnaire:**
  - Violence: No
  - Sexual content: No
  - Profanity: No
  - Controlled substances: No
  - Rating result: **Everyone / PEGI 3**.

#### 3. Privacy Policy URL
- Host `privacy_policy.html` on GitHub Pages (e.g. `https://yourusername.github.io/antiphonesnatcher/privacy_policy.html`) or any static hosting, and paste the URL in:
  **Policy > App content > Privacy policy**.

#### 4. Accessibility Tool Declaration (`BIND_ACCESSIBILITY_SERVICE`)
Google Play strictly requires declaring why Accessibility is used. Use these exact answers:

- **Is your app an Accessibility Tool (designed for users with disabilities)?**
  Select **No**.
- **Why does your app use Accessibility Services?**
  Select **Other (Security / Hardware triggers)** and provide this explanation:
  ```text
  Anti Phone Snatcher uses AccessibilityService solely to provide two on-device physical security features:
  1. Detect hardware volume button presses (double-pressing Volume Down) to trigger an immediate privacy blackout curtain.
  2. Protect users against phone snatching by detecting unauthorized notification shade pull-downs or settings tampering while armed in blackout mode, locking the device instantly.
  Anti Phone Snatcher does NOT access, read, log, store, or transmit any typed text, passwords, screen content, or user communications.
  ```
- **Demo Video URL (YouTube):**
  Google Play requires a short unlisted YouTube video showing the accessibility features:
  1. Open Anti Phone Snatcher -> Tap "Anti-Tamper Shield" -> Notice the Prominent In-App Disclosure -> Tap "Agree & Enable".
  2. Double-press Volume Down to trigger blackout.
  3. Enter PIN / Tap sequence to unlock.
  *(Keep video unlisted on YouTube and paste the link in the declaration field).*

#### 5. Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)
- Justification:
  ```text
  Required to render the privacy shield / blackout curtain over the user's active screen when triggered by the user or sudden motion detection.
  ```

---

## 3. Production Release Files
The build outputs generated for submission:
1. **Google Play App Bundle (.aab)**:
   `build-output/AntiPhoneSnatcher-release.aab`
   *(Upload this file directly to Google Play Console > Production > Create new release)*
2. **Signed Release APK (.apk)**:
   `build-output/AntiPhoneSnatcher-release.apk`
   *(For testing on your physical phone before publishing)*

3. **Package Name & Keystore Details**:
   - **Package Name (Application ID):** `com.antiphonesnatcher.app`
   - **Google Play URL:** `https://play.google.com/store/apps/details?id=com.antiphonesnatcher.app`
   - **Keystore location:** `app/antiphonesnatcher-release.jks`
   - **Alias:** `antiphonesnatcher`
   - **Key & Store Password:** `antiphonesnatcher2026!`
   - **Validity:** 10,000 days (RSA 2048-bit)
