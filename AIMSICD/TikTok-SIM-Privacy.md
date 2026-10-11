## TikTok SIM/Telephony Privacy (Non‑root)

This guide explains practical steps to prevent apps (e.g., TikTok) from learning SIM/phone identifiers on Android devices without root.

### 1) Deny phone/SIM permissions
- Settings → Apps → TikTok → Permissions → Phone → Deny. Also deny SMS/Call log if present.
- Force stop TikTok, then Clear storage.

### 2) Stronger enforcement with ADB (no root)
```bash
# Replace PKG with the exact package of TikTok (e.g., com.zhiliaoapp.musically or com.ss.android.ugc.trill)
PKG=com.zhiliaoapp.musically

# Revoke telephony-related dangerous permissions
adb shell pm revoke $PKG android.permission.READ_PHONE_STATE
adb shell pm revoke $PKG android.permission.READ_PHONE_NUMBERS
adb shell pm revoke $PKG android.permission.READ_SMS
adb shell pm revoke $PKG android.permission.READ_CALL_LOG

# Clamp app-ops (Android 10+)
adb shell cmd appops set $PKG READ_DEVICE_IDENTIFIERS ignore
adb shell cmd appops set $PKG READ_PHONE_STATE ignore
adb shell cmd appops set $PKG READ_PHONE_NUMBERS ignore
adb shell cmd appops set $PKG READ_SMS ignore
adb shell cmd appops set $PKG READ_CALL_LOG ignore

# Reset the app
adb shell am force-stop $PKG
adb shell pm clear $PKG
```

### 3) Use a secondary Android user without telephony
- Settings → System → Multiple users → Add user.
- Do not enable “Phone calls & SMS for this user.”
- Switch, install TikTok, use Wi‑Fi.

### 4) Temporarily disable the SIM subscription
- Settings → Network & Internet → SIMs → toggle OFF the problematic SIM.
- Keep Wi‑Fi ON. Force stop + Clear storage for TikTok before testing.

### Notes
- Intercepting app traffic is not feasible non‑root due to TLS pinning.
- Use the new “Privacy Advisor” (App → Menu → Privacy Advisor) to see which installed apps request telephony permissions and copy ready‑made ADB commands.


