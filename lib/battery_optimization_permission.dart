import 'dart:io' show Platform;

import 'package:flutter/services.dart';

/// Battery Optimization / Doze + OEM "Auto start / Background" helpers.
class BatteryOptimizationPermission {
  static const MethodChannel _channel = MethodChannel('battery_optimization_permission');

  /// Android: true if ignoring battery optimizations (Doze whitelist).
  /// Android < 6.0: true (no Doze).
  static Future<bool> isIgnoringBatteryOptimizations() async {
    if (!Platform.isAndroid) return false;
    try {
      final res = await _channel.invokeMethod<bool>('isIgnoringBatteryOptimizations');
      return res ?? false;
    } on PlatformException {
      return false;
    }
  }

  /// Android: show system prompt to whitelist from Doze optimizations.
  /// Returns true if granted (or already granted).
  static Future<bool> requestIgnoreBatteryOptimizations() async {
    if (!Platform.isAndroid) return false;
    try {
      final res = await _channel.invokeMethod<bool>('requestIgnoreBatteryOptimizations');
      return res ?? false;
    } on PlatformException {
      return false;
    }
  }

  /// Opens Android's battery optimization list screen.
  static Future<void> openBatteryOptimizationSettings() async {
    if (!Platform.isAndroid) return;
    try {
      await _channel.invokeMethod('openBatteryOptimizationSettings');
    } on PlatformException {
      // ignore
    }
  }

  /// Opens this app's settings page (Android + iOS supported in plugin).
  static Future<void> openAppSettings() async {
    try {
      await _channel.invokeMethod('openAppSettings');
    } on PlatformException {
      // ignore
    }
  }

  /// Tries to open OEM-specific "Auto start / Background activity" settings.
  /// Returns true if an OEM page was opened, else false.
  static Future<bool> openOemAutoStartSettings() async {
    if (!Platform.isAndroid) return false;
    try {
      final res = await _channel.invokeMethod<bool>('openOemAutoStartSettings');
      return res ?? false;
    } on PlatformException {
      return false;
    }
  }

  /// Best UX single-call helper:
  /// 1) If already whitelisted -> true
  /// 2) Request prompt -> if granted -> true
  /// 3) If denied -> tries OEM screen -> then Battery Optimization settings -> then App settings
  /// Returns true only if we end up whitelisted (now), else false.
  static Future<bool> ensureBatteryWhitelist({
    bool tryOemScreens = true,
    bool openSettingsFallbacks = true,
  }) async {
    if (!Platform.isAndroid) return false;

    // 1) already ok
    if (await isIgnoringBatteryOptimizations()) return true;

    // 2) prompt
    final granted = await requestIgnoreBatteryOptimizations();
    if (granted) return true;

    // 3) fallbacks (we can't "wait" inside this method; we open the best pages)
    if (openSettingsFallbacks) {
      if (tryOemScreens) {
        final opened = await openOemAutoStartSettings();
        if (opened) return false;
      }

      // general settings list
      await openBatteryOptimizationSettings();

      // final fallback - app details
      await openAppSettings();
    }

    // Re-check (may still be false until user enables)
    return await isIgnoringBatteryOptimizations();
  }
}
