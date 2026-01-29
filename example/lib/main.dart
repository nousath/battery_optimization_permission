import 'dart:io';
import 'package:flutter/material.dart';
import 'package:battery_optimization_permission/battery_optimization_permission.dart';

void main() => runApp(const MyApp());

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Battery Optimization Permission',
      home: const HomePage(),
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.blue),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _status = 'Ready';

  Future<void> _set(String s) async {
    setState(() => _status = s);
  }

  @override
  Widget build(BuildContext context) {
    final isAndroid = Platform.isAndroid;

    return Scaffold(
      appBar: AppBar(title: const Text('Battery Optimization')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  'Status: $_status',
                  style: const TextStyle(fontWeight: FontWeight.w700),
                ),
              ),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: !isAndroid
                  ? null
                  : () async {
                      final v = await BatteryOptimizationPermission.isIgnoringBatteryOptimizations();
                      await _set('Ignoring battery optimizations: $v');
                    },
              child: const Text('Check isIgnoringBatteryOptimizations'),
            ),
            const SizedBox(height: 10),
            FilledButton.tonal(
              onPressed: !isAndroid
                  ? null
                  : () async {
                      final v = await BatteryOptimizationPermission.requestIgnoreBatteryOptimizations();
                      await _set('Request result: $v');
                    },
              child: const Text('Request ignore battery optimizations'),
            ),
            const SizedBox(height: 10),
            FilledButton.tonal(
              onPressed: !isAndroid
                  ? null
                  : () async {
                      final v = await BatteryOptimizationPermission.ensureBatteryWhitelist();
                      await _set('ensureBatteryWhitelist(): $v');
                    },
              child: const Text('Ensure whitelist (best UX)'),
            ),
            const SizedBox(height: 10),
            OutlinedButton(
              onPressed: !isAndroid
                  ? null
                  : () async {
                      final opened = await BatteryOptimizationPermission.openOemAutoStartSettings();
                      await _set('OEM auto-start screen opened: $opened');
                    },
              child: const Text('Open OEM auto-start settings'),
            ),
            const SizedBox(height: 10),
            OutlinedButton(
              onPressed: () async {
                await BatteryOptimizationPermission.openBatteryOptimizationSettings();
                await _set('Opened battery optimization settings');
              },
              child: const Text('Open battery optimization settings'),
            ),
            const SizedBox(height: 10),
            OutlinedButton(
              onPressed: () async {
                await BatteryOptimizationPermission.openAppSettings();
                await _set('Opened app settings');
              },
              child: const Text('Open app settings'),
            ),
            const Spacer(),
            Text(
              isAndroid
                  ? 'Tip: After enabling, come back and press "Check".'
                  : 'Android-only (iOS unsupported for Doze).',
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }
}
