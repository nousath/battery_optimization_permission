package `in`.co.nh97.battery_optimization_permission

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener
import java.util.Locale

class BatteryOptimizationPermissionPlugin : FlutterPlugin, MethodChannel.MethodCallHandler,
    ActivityAware, ActivityResultListener {

    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private var activity: Activity? = null

    private var pendingResult: Result? = null
    private val REQ_IGNORE_BATTERY_OPT = 8207

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        applicationContext = binding.applicationContext
        channel = MethodChannel(binding.binaryMessenger, "battery_optimization_permission")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        applicationContext = null
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "isIgnoringBatteryOptimizations" -> result.success(isIgnoringBatteryOptimizations())
            "requestIgnoreBatteryOptimizations" -> requestIgnoreBatteryOptimizations(result)
            "openBatteryOptimizationSettings" -> openBatteryOptimizationSettings(result)
            "openAppSettings" -> openAppSettings(result)
            "openOemAutoStartSettings" -> openOemAutoStartSettings(result)
            else -> result.notImplemented()
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val ctx = applicationContext ?: return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(ctx.packageName)
    }

    private fun requestIgnoreBatteryOptimizations(result: Result) {
        val ctx = applicationContext
        val act = activity

        if (ctx == null) {
            result.error("NO_CONTEXT", "Application context is null", null)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            result.success(true)
            return
        }

        val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
        val pkg = ctx.packageName

        if (pm.isIgnoringBatteryOptimizations(pkg)) {
            result.success(true)
            return
        }

        if (act == null) {
            result.error("NO_ACTIVITY", "Plugin requires a foreground activity", null)
            return
        }

        if (pendingResult != null) {
            result.error("ALREADY_REQUESTING", "A request is already in progress", null)
            return
        }

        pendingResult = result

        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$pkg")
            act.startActivityForResult(intent, REQ_IGNORE_BATTERY_OPT)
        } catch (e: Exception) {
            pendingResult = null
            result.error("REQUEST_FAILED", e.message, null)
        }
    }

    private fun openBatteryOptimizationSettings(result: Result) {
        val act = activity ?: run {
            result.error("NO_ACTIVITY", "Plugin requires a foreground activity", null)
            return
        }
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            act.startActivity(intent)
            result.success(true)
        } catch (e: Exception) {
            result.error("OPEN_SETTINGS_FAILED", e.message, null)
        }
    }

    private fun openAppSettings(result: Result) {
        val ctx = applicationContext ?: run {
            result.error("NO_CONTEXT", "Application context is null", null)
            return
        }
        val act = activity ?: run {
            result.error("NO_ACTIVITY", "Plugin requires a foreground activity", null)
            return
        }

        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", ctx.packageName, null)
            act.startActivity(intent)
            result.success(true)
        } catch (e: Exception) {
            result.error("OPEN_APP_SETTINGS_FAILED", e.message, null)
        }
    }

    // ---------------- OEM Auto-start / Background pages ----------------

    private fun openOemAutoStartSettings(result: Result) {
        val ctx = applicationContext ?: run {
            result.error("NO_CONTEXT", "Application context is null", null)
            return
        }
        val act = activity ?: run {
            result.error("NO_ACTIVITY", "Plugin requires a foreground activity", null)
            return
        }

        val manufacturer = (Build.MANUFACTURER ?: "").lowercase(Locale.getDefault())
        val pkg = ctx.packageName

        val candidates = mutableListOf<Intent>()

        // Xiaomi / Redmi / Poco (MIUI / HyperOS)
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains(
                "poco"
            )
        ) {
            // MIUI AutoStart manager (commonly used)
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                )
            )

            // MIUI hidden apps battery restrictions page (may be blocked on some Android 13+ builds)
            val miuiHidden = Intent().setComponent(
                ComponentName(
                    "com.miui.powerkeeper",
                    "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                )
            )
            miuiHidden.putExtra("package_name", pkg)
            miuiHidden.putExtra("package_label", getAppLabel(ctx))
            candidates.add(miuiHidden)

            // Some MIUI action intents
            candidates.add(Intent("miui.intent.action.OP_AUTO_START"))
            candidates.add(Intent("miui.intent.action.POWER_HIDE_MODE_APP_LIST"))
        }

        // OPPO / Realme / OnePlus (ColorOS-derived)
        if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains(
                "oneplus"
            )
        ) {
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.startupapp.StartupAppListActivity"
                    )
                )
            )
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                )
            )
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity"
                    )
                )
            )
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.coloros.oppoguardelf",
                        "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"
                    )
                )
            )
        }

        // Vivo / iQOO
        if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                )
            )
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
                )
            )
        }

        // Samsung (Device Care / Battery)
        if (manufacturer.contains("samsung")) {
            candidates.add(
                Intent().setComponent(
                    ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                )
            )
            // General battery saver settings fallback
            candidates.add(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
        }

        // Try start first workable candidate
        for (intent in candidates) {
            if (tryStartActivity(act, intent)) {
                result.success(true)
                return
            }
        }

        // nothing worked
        result.success(false)
    }

    private fun tryStartActivity(act: Activity, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pm = act.packageManager
            val resolved = pm.resolveActivity(intent, 0)
            if (resolved != null) {
                act.startActivity(intent)
                true
            } else {
                false
            }
        } catch (_: SecurityException) {
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun getAppLabel(ctx: Context): String {
        return try {
            val pm = ctx.packageManager
            val ai = pm.getApplicationInfo(ctx.packageName, 0)
            pm.getApplicationLabel(ai).toString()
        } catch (_: Exception) {
            "App"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQ_IGNORE_BATTERY_OPT) return false
        val r = pendingResult
        pendingResult = null
        r?.success(isIgnoringBatteryOptimizations())
        return true
    }
}
