package com.example.prtracker.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.prtracker.MainActivity
import com.example.prtracker.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

class RunTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var locationCallback: LocationCallback? = null
    private var tickerJob: Job? = null
    private var lastLocation: Location? = null
    private val kalmanFilter = KalmanFilter2D()

    companion object {
        val distanceMeters = MutableStateFlow(0f)
        val elapsedSeconds = MutableStateFlow(0L)
        val currentPaceSecPerKm = MutableStateFlow<Float?>(null)
        val isTracking = MutableStateFlow(false)
        val isPaused = MutableStateFlow(false)

        const val WARMUP_DURATION_MS = 8_000L
        var trackingStartTimeMs: Long = 0L

        fun reset() {
            distanceMeters.value = 0f
            elapsedSeconds.value = 0L
            currentPaceSecPerKm.value = null
            isTracking.value = false
            isPaused.value = false
            trackingStartTimeMs = 0L
        }

        fun start(context: Context) {
            val intent = Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, RunTrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        private const val ACTION_START = "com.example.prtracker.action.START"
        private const val ACTION_PAUSE = "com.example.prtracker.action.PAUSE"
        private const val ACTION_RESUME = "com.example.prtracker.action.RESUME"
        private const val ACTION_STOP = "com.example.prtracker.action.STOP"
        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "pr_tracker_running"
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_STOP -> handleStop()
        }
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun handleStart() {
        reset()
        trackingStartTimeMs = System.currentTimeMillis()
        kalmanFilter.reset()
        isTracking.value = true
        isPaused.value = false
        lastLocation = null
        startForeground(NOTIFICATION_ID, buildNotification())
        startLocationUpdates()
        startTicker()
    }

    @SuppressLint("MissingPermission")
    private fun handlePause() {
        isPaused.value = true
        fusedLocationClient.removeLocationUpdates(locationCallback!!)
        updateNotification()
    }

    @SuppressLint("MissingPermission")
    private fun handleResume() {
        isPaused.value = false
        startLocationUpdates()
        updateNotification()
    }

    @SuppressLint("MissingPermission")
    private fun handleStop() {
        isTracking.value = false
        isPaused.value = false
        tickerJob?.cancel()
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    if (System.currentTimeMillis() - trackingStartTimeMs < WARMUP_DURATION_MS) continue
                    if (location.accuracy > 20f) continue

                    val delta = lastLocation?.distanceTo(location) ?: 0f
                    if (lastLocation != null && delta <= 2f) continue

                    val (kLat, kLon) = kalmanFilter.update(
                        location.latitude,
                        location.longitude,
                        location.time
                    )

                    val filteredDelta = if (lastLocation != null) {
                        val d = FloatArray(1)
                        Location.distanceBetween(
                            lastLocation!!.latitude,
                            lastLocation!!.longitude,
                            kLat,
                            kLon,
                            d
                        )
                        d[0]
                    } else {
                        0f
                    }

                    distanceMeters.value = distanceMeters.value + filteredDelta

                    val filteredLocation = Location(location).apply {
                        latitude = kLat
                        longitude = kLon
                    }
                    lastLocation = filteredLocation

                    val dist = distanceMeters.value
                    val elapsed = elapsedSeconds.value
                    if (dist > 100f && elapsed > 0) {
                        currentPaceSecPerKm.value = elapsed / (dist / 1000f)
                    }
                    updateNotification()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L)
                if (!isPaused.value) {
                    elapsedSeconds.value = elapsedSeconds.value + 1L
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Run Tracker",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Live run tracking notification"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, RunTrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(
                this, 0, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this, 1, openIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getActivity(this, 1, openIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val distText = formatDistance(distanceMeters.value)
        val timeText = formatElapsed(elapsedSeconds.value)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RUN IN PROGRESS")
            .setContentText("$distText • $timeText")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "STOP", stopPendingIntent)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification())
    }

    private fun formatDistance(meters: Float): String {
        return if (meters >= 1000f) {
            "%.2f km".format(meters / 1000f)
        } else {
            "%.0f m".format(meters)
        }
    }

    private fun formatElapsed(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    private class KalmanFilter2D {
        private val x = DoubleArray(4)
        private val p = DoubleArray(16)
        private var lastTimeMs: Long = 0L

        companion object {
            private const val Q = 0.0001
            private const val R_DEG = 0.00003
        }

        fun reset() {
            for (i in 0..15) p[i] = 0.0
            p[0] = 1.0; p[5] = 1.0; p[10] = 1.0; p[15] = 1.0
            x[0] = 0.0; x[1] = 0.0; x[2] = 0.0; x[3] = 0.0
            lastTimeMs = 0L
        }

        private fun initialize(lat: Double, lon: Double, timeMs: Long) {
            x[0] = lat; x[1] = lon; x[2] = 0.0; x[3] = 0.0
            for (i in 0..15) p[i] = 0.0
            p[0] = 1.0; p[5] = 1.0; p[10] = 1.0; p[15] = 1.0
            lastTimeMs = timeMs
        }

        fun update(lat: Double, lon: Double, timeMs: Long): Pair<Double, Double> {
            if (lastTimeMs == 0L) {
                initialize(lat, lon, timeMs)
                return Pair(lat, lon)
            }

            val dt = ((timeMs - lastTimeMs).coerceAtLeast(1L)) / 1000.0
            lastTimeMs = timeMs
            predict(dt)

            val y0 = lat - x[0]
            val y1 = lon - x[1]

            val s00 = p[0] + R_DEG
            val s01 = p[1]
            val s10 = p[4]
            val s11 = p[5] + R_DEG

            val det = s00 * s11 - s01 * s10
            if (abs(det) < 1e-30) return Pair(x[0], x[1])

            val invDet = 1.0 / det
            val invS00 = s11 * invDet
            val invS01 = -s01 * invDet
            val invS10 = -s10 * invDet
            val invS11 = s00 * invDet

            val k00 = p[0] * invS00 + p[1] * invS10
            val k01 = p[0] * invS01 + p[1] * invS11
            val k10 = p[4] * invS00 + p[5] * invS10
            val k11 = p[4] * invS01 + p[5] * invS11
            val k20 = p[8] * invS00 + p[9] * invS10
            val k21 = p[8] * invS01 + p[9] * invS11
            val k30 = p[12] * invS00 + p[13] * invS10
            val k31 = p[12] * invS01 + p[13] * invS11

            x[0] += k00 * y0 + k01 * y1
            x[1] += k10 * y0 + k11 * y1
            x[2] += k20 * y0 + k21 * y1
            x[3] += k30 * y0 + k31 * y1

            val p00 = p[0]; val p01 = p[1]; val p02 = p[2]; val p03 = p[3]
            val p10 = p[4]; val p11 = p[5]; val p12 = p[6]; val p13 = p[7]
            val p20 = p[8]; val p21 = p[9]; val p22 = p[10]; val p23 = p[11]
            val p30 = p[12]; val p31 = p[13]; val p32 = p[14]; val p33 = p[15]

            p[0] = p00 - (k00 * p00 + k01 * p10)
            p[1] = p01 - (k00 * p01 + k01 * p11)
            p[2] = p02 - (k00 * p02 + k01 * p12)
            p[3] = p03 - (k00 * p03 + k01 * p13)

            p[4] = p10 - (k10 * p00 + k11 * p10)
            p[5] = p11 - (k10 * p01 + k11 * p11)
            p[6] = p12 - (k10 * p02 + k11 * p12)
            p[7] = p13 - (k10 * p03 + k11 * p13)

            p[8] = p20 - (k20 * p00 + k21 * p10)
            p[9] = p21 - (k20 * p01 + k21 * p11)
            p[10] = p22 - (k20 * p02 + k21 * p12)
            p[11] = p23 - (k20 * p03 + k21 * p13)

            p[12] = p30 - (k30 * p00 + k31 * p10)
            p[13] = p31 - (k30 * p01 + k31 * p11)
            p[14] = p32 - (k30 * p02 + k31 * p12)
            p[15] = p33 - (k30 * p03 + k31 * p13)

            return Pair(x[0], x[1])
        }

        private fun predict(dtSeconds: Double) {
            val dt = dtSeconds

            x[0] += dt * x[2]
            x[1] += dt * x[3]

            val p00 = p[0]; val p01 = p[1]; val p02 = p[2]; val p03 = p[3]
            val p10 = p[4]; val p11 = p[5]; val p12 = p[6]; val p13 = p[7]
            val p20 = p[8]; val p21 = p[9]; val p22 = p[10]; val p23 = p[11]
            val p30 = p[12]; val p31 = p[13]; val p32 = p[14]; val p33 = p[15]

            val fp00 = p00 + dt * p20; val fp01 = p01 + dt * p21; val fp02 = p02 + dt * p22; val fp03 = p03 + dt * p23
            val fp10 = p10 + dt * p30; val fp11 = p11 + dt * p31; val fp12 = p12 + dt * p32; val fp13 = p13 + dt * p33
            val fp20 = p20; val fp21 = p21; val fp22 = p22; val fp23 = p23
            val fp30 = p30; val fp31 = p31; val fp32 = p32; val fp33 = p33

            p[0] = fp00 + dt * fp02; p[1] = fp01 + dt * fp03; p[2] = fp02; p[3] = fp03
            p[4] = fp10 + dt * fp12; p[5] = fp11 + dt * fp13; p[6] = fp12; p[7] = fp13
            p[8] = fp20 + dt * fp22; p[9] = fp21 + dt * fp23; p[10] = fp22; p[11] = fp23
            p[12] = fp30 + dt * fp32; p[13] = fp31 + dt * fp33; p[14] = fp32; p[15] = fp33

            p[0] += Q; p[5] += Q; p[10] += Q; p[15] += Q
        }
    }
}
