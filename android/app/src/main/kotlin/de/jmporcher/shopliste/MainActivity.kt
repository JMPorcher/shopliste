package de.jmporcher.shopliste

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    private val CHANNEL = "de.jmporcher.shopliste/data"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            if (call.method == "getSharedData") {
                // Holt das Extra (z.B. "Hafermilch") aus dem Android-Intent
                val sharedData = intent.getStringExtra("item")
                result.success(sharedData)
                // Extra löschen, damit es beim manuellen App-Wechsel nicht erneut triggert
                intent.removeExtra("item")
            } else {
                result.notImplemented()
            }
        }
    }
}