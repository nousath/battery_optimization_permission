<p align="center">
  <img src="assets/nh97_logo.png" width="120" alt="NH97" />
</p>

<h1 align="center">battery_optimization_permission</h1>

<p align="center">
  Android battery optimization (Doze) whitelist helper + best‑effort OEM Auto‑start / Background settings shortcuts.
  <br/>
  <a href="https://nh97.co.in">nh97.co.in</a>
</p>

---

## ✨ Features

- ✅ Check if the app is **whitelisted** (ignoring battery optimizations) on Android 6.0+
- ⚙️ Prompt the user to allow **“Ignore battery optimizations”**
- ⚙️ Open system **battery optimization settings**
- ⚙️ Open **app settings**
- 🧩 Best‑effort OEM Auto‑start / Background settings deep links:
    - Xiaomi / Redmi / Poco (MIUI / HyperOS)
    - OPPO / Realme / OnePlus (ColorOS family)
    - Vivo / iQOO
    - Samsung
- ✅ One call flow (**best UX**): **prompt → OEM screen → system settings → app settings**

---

## Installing

Add to `pubspec.yaml`:

```yaml
dependencies:
  battery_optimization_permission: ^1.1.0
```

Or use the CLI:

```bash
flutter pub add battery_optimization_permission
```

Then run `flutter pub get` if needed.

### Android manifest permission

This plugin declares:

```xml
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
```

If you prefer to declare it yourself, keep the same permission in your app manifest.

---

## Usage

```dart
import 'dart:io';
import 'package:battery_optimization_permission/battery_optimization_permission.dart';

Future<void> example() async {
  if (!Platform.isAndroid) return;

  // Check
  final whitelisted =
      await BatteryOptimizationPermission.isIgnoringBatteryOptimizations();

  if (!whitelisted) {
    // Best UX: prompt → OEM screen → settings fallbacks
    final ok = await BatteryOptimizationPermission.ensureBatteryWhitelist(
      tryOemScreens: true,
      openSettingsFallbacks: true,
    );

    // ok == true only if the app is whitelisted right now.
    // If ok == false, settings screens were opened for the user to complete manually.
  }

  // Open system battery optimization screen
  await BatteryOptimizationPermission.openBatteryOptimizationSettings();

  // Best‑effort OEM background/auto‑start settings (may return false)
  final opened = await BatteryOptimizationPermission.openOemAutoStartSettings();
  if (!opened) {
    // Consider showing your in‑app guidance / help page
  }
}
```

### Result‑based behavior

- `requestIgnoreBatteryOptimizations()` returns the **current state** after the user returns from the system dialog.
- `ensureBatteryWhitelist()` returns `true` only if your app is **already whitelisted** or becomes whitelisted via the prompt.

---

## Notes on Android versions

- **Android < 6.0 (API < 23):** Doze/battery optimizations don’t apply. The check safely reports whitelisted.
- **OEM Auto‑start settings:** Paths differ by device/ROM and may not exist. OEM shortcuts are **best‑effort**, and the plugin falls back to system settings or app settings.

---

## Tooling

Recommended (matches this plugin template):

- Kotlin Gradle Plugin: **1.9.x**
- Android Gradle Plugin: **8.3+**
- compileSdk: **34**
- Flutter: **3.x**

---

## Tips

- Explain clearly **why** the whitelist is required (reminders, attendance/shift alerts, background services) before showing the system prompt.
- Handle `false` results from `openOemAutoStartSettings()` gracefully.
- If `ensureBatteryWhitelist()` returns `false`, show a short step list in your UI (OEM + system screens).

---

## Example

See `example/` for a runnable app.

---

## Changelog

See `CHANGELOG.md` for release notes.

---

## License

MIT

---

## ☕ Sponsor a cup of tea

If this package saves you development time, consider supporting my work.

[![Sponsor](https://img.shields.io/badge/Sponsor%20on%20GitHub-%E2%98%95-blue?logo=githubsponsors)](https://github.com/sponsors/nousath)

https://github.com/sponsors/nousath