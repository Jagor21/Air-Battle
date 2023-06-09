package com.viridi_domo.airbattle

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.google.firebase.FirebaseApp
import com.viridi_domo.airbattle.AirBattleAFAnalytics.Companion.FIRST_LAUNCH_EVENT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

private const val LOG_TAG = "AirBattleApp"
private const val FIRST_LAUNCH = "first_launch"

class AirBattleApp : Application() {

    private lateinit var sharedPrefEditor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var appsFlyerAnalytics: AirBattleAFAnalytics

    val conversionData = MutableStateFlow<Any?>(null)

    override fun onCreate() {
        super.onCreate()
        createNotificationsChannels()
        createSilentNotificationChannel()

        sharedPreferences = getSharedPreferences("your_prefs", MODE_PRIVATE)
        sharedPrefEditor = sharedPreferences.edit()

//        Tracker.getInstance().startWithAppGuid(this, getString(R.string.app_guid_kochava))

        FirebaseApp.initializeApp(this)
//        Tracker.getInstance().setLogLevel(LogLevel.DEBUG)
//        val currentInstallAttribution = Tracker.getInstance().installAttribution
//        if(!currentInstallAttribution.isRetrieved) {
//            Tracker.getInstance().retrieveInstallAttribution { installAttribution ->
//                Log.d("KOCHAVA", installAttribution.toJson().toString())
//            }
//        } else {
//            Log.d("KOCHAVA", currentInstallAttribution.toJson().toString())
//        }
        val conversionDataListener = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                Log.d(LOG_TAG, data.toString())
                Log.d(LOG_TAG, data?.get("campaign").toString())
                sharedPrefEditor.putString("conversion_data", data?.get("campaign").toString())
                    .commit()
                conversionData.update { data?.get("campaign") }
            }

            override fun onConversionDataFail(error: String?) {
                Log.e(LOG_TAG, "error onAttributionFailure :  $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                // Must be overriden to satisfy the AppsFlyerConversionListener interface.
                // Business logic goes here when UDL is not implemented.
                data?.map {
                    Log.d(LOG_TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
                }
            }

            override fun onAttributionFailure(error: String?) {
                // Must be overriden to satisfy the AppsFlyerConversionListener interface.
                // Business logic goes here when UDL is not implemented.
                Log.e(LOG_TAG, "error onAttributionFailure :  $error")
            }
        }
        AppsFlyerLib.getInstance().setDebugLog(true)
        AppsFlyerLib.getInstance()
            .init(getString(R.string.appsflyer_dev_key), conversionDataListener, this)
        AppsFlyerLib.getInstance()
            .start(this, getString(R.string.appsflyer_dev_key), object : AppsFlyerRequestListener {
                override fun onSuccess() {
                    Log.d(LOG_TAG, "Appsflyer started!")
                }

                override fun onError(i: Int, s: String) {
                    Log.e(
                        LOG_TAG,
                        "Launch failed to be sent: Error code: $i Error description: $s".trimIndent()
                    )
                }
            })
        appsFlyerAnalytics = AirBattleAFAnalytics(applicationContext)
        if (sharedPreferences.getBoolean(FIRST_LAUNCH, true)) {
            appsFlyerAnalytics.logEvent(FIRST_LAUNCH_EVENT)
            sharedPrefEditor?.putBoolean(FIRST_LAUNCH, false)?.commit()
        }

    }

    private fun createNotificationsChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.reminders_notification_channel_id),
                getString(R.string.reminders_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            ContextCompat.getSystemService(this, NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    private fun createSilentNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(AirBattleFCMService.SILENT_CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    AirBattleFCMService.SILENT_CHANNEL_ID,
                    "WheelOfFortune silent events",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.enableLights(false)
                channel.enableVibration(false)
                channel.setSound(null, null)
                manager.createNotificationChannel(channel)
            }
        }
    }
}