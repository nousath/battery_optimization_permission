# battery_optimization_permission

Android battery optimization (Doze) whitelist helper with OEM deep-links (auto start / background activity pages) and robust fallbacks.

Homepage: https://nh97.co.in

## Features
- Check if the app is ignoring battery optimizations (Doze whitelist)
- Request ignore battery optimizations (system prompt)
- Open battery optimization settings list
- Open app settings screen
- OEM deep links (best-effort): Xiaomi/Redmi/Poco, OPPO/Realme/OnePlus, Vivo/iQOO, Samsung

## Android setup
The plugin declares:
- `android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`

## Usage

```dart
import 'dart:io';
import 'package:battery_optimization_permission/battery_optimization_permission.dart';

Future<void> setupBattery() async {
  if (!Platform.isAndroid) return;

  final ok = await BatteryOptimizationPermission.ensureBatteryWhitelist(
    tryOemScreens: true,
    openSettingsFallbacks: true,
  );

  // ok == true only if already whitelisted, or user granted via system prompt.
  // If user denies prompt, we open best settings pages for manual enabling.
}
```

### Direct OEM page (optional)

```dart
final opened = await BatteryOptimizationPermission.openOemAutoStartSettings();
```

## Notes
- Doze exists on Android 6.0+; Android < 6 returns `true` for `isIgnoringBatteryOptimizations()`.
- OEM pages vary across ROM versions; plugin tries multiple known components and falls back safely.
