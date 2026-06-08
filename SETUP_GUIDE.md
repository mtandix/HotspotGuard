# 📡 HotspotGuard - Setup Guide
### Samsung M35 5G | Android 12 | Windows 11

---

## STEP 1 — Install Required Apps on Phone

1. Open **Play Store**
2. Install **Shizuku** (by Rikka, free)
3. That's it for now!

---

## STEP 2 — Enable Developer Options on Phone

1. Go to **Settings → About Phone**
2. Tap **Software Information**
3. Tap **Build Number** 7 times rapidly
4. You'll see "Developer mode enabled"
5. Go back → **Settings → Developer Options**
6. Turn it **ON**
7. Enable **USB Debugging**

---

## STEP 3 — Install Android Studio on Laptop (Windows 11)

1. Download from: **https://developer.android.com/studio**
2. Install with default settings
3. During setup, let it download Android SDK automatically

---

## STEP 4 — Build the APK

1. Open **Android Studio**
2. Click **"Open"** → Select the **HotspotGuard** folder
3. Wait for Gradle sync to complete (2-5 minutes, needs internet)
4. Click **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. Wait... APK will be built at:
   `HotspotGuard/app/build/outputs/apk/debug/app-debug.apk`

---

## STEP 5 — Install APK on Phone

**Option A — Via Android Studio (Easiest):**
1. Connect phone to laptop via USB cable
2. On phone: Allow USB connection → select "File Transfer"
3. In Android Studio: click the **Play ▶ button** (Run)
4. Select your Samsung M35 5G
5. App installs automatically!

**Option B — Manual:**
1. Copy `app-debug.apk` to phone
2. On phone: Settings → Install Unknown Apps → Allow
3. Open the APK file → Install

---

## STEP 6 — Setup Shizuku (ONE TIME ONLY!)

1. Connect phone to laptop via USB
2. Open **Command Prompt** on laptop (Win + R → type `cmd`)
3. Type this command:
```
adb devices
```
4. On phone: tap **"Allow"** for USB Debugging popup
5. Now type:
```
adb shell sh /storage/emulated/0/Android/data/moe.shizuku.privileged.api/start.sh
```
6. Open **Shizuku app** on phone
7. You'll see "Shizuku is running" ✅

---

## STEP 7 — Grant Permission to HotspotGuard

1. Open **Shizuku app**
2. Go to **"Authorized apps"**
3. Find **HotspotGuard** → tap **Authorize**
4. Done! ✅

---

## HOW TO USE THE APP

1. Make sure **Shizuku is running** (open Shizuku app to check)
2. Open **HotspotGuard**
3. Press **"▶ Start Monitoring"**
4. App runs in background with a notification
5. Share hotspot with anyone normally
6. **When 5G drops → hotspot turns OFF automatically**
7. **When 5G returns → hotspot turns ON automatically**
8. To stop: Open app → Press **"Stop Monitoring"**

---

## TROUBLESHOOTING

| Problem | Solution |
|---------|----------|
| "Shizuku not running" | Open Shizuku app → check status |
| Hotspot not turning off | Re-authorize app in Shizuku |
| App crashing | Restart Shizuku, then reopen app |
| ADB not found | Reinstall Android Studio |

---

## NOTE ABOUT SHIZUKU AFTER RESTART

⚠️ Shizuku resets on phone restart!
After restarting your phone, you need to:
1. Connect to laptop via USB
2. Run the adb command again (Step 6, command #5)
3. Or: In Shizuku app → Use "Wireless debugging" mode (no USB needed after first setup)

---

*Built for Manish | HotspotGuard v1.0*
