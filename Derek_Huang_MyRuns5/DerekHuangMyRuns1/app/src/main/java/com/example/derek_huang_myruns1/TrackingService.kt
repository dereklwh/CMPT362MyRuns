package com.example.derek_huang_myruns1

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.sqrt

class TrackingService : Service(), SensorEventListener {

    private lateinit var locationManager: LocationManager
    private lateinit var sensorManager: SensorManager
    private val locationListener: LocationListener = LocationListener { location ->
        // Update location data to LiveData
        locationUpdates.postValue(location)
    }
    private val sensorDataQueue = LinkedBlockingQueue<Array<Double>>()
    private val executorService = Executors.newSingleThreadExecutor()

    companion object {
        val locationUpdates: MutableLiveData<Location> = MutableLiveData()
        val activityTypeUpdates: MutableLiveData<String> = MutableLiveData()
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return the communication channel to the service
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, getNotification())
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        startDataProcessingThread()
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10f, locationListener)
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, getNotification())
        startLocationUpdates()
        startSensorUpdates()
        return START_STICKY
    }

    //SENSORS FOR LAB 5
    private fun startSensorUpdates() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopSensorUpdates() {
        sensorManager.unregisterListener(this)
    }

    private fun startDataProcessingThread() {
        executorService.execute {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    val featureVector = ArrayList<Double>()
                    for (i in 0 until Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        featureVector.addAll(sensorDataQueue.take())
                    }
                    // Classify activity and post to LiveData
                    classifyActivityWithWeka(featureVector)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            Log.d("SensorChange", "Accelerometer data: ${event.values.contentToString()}")
            val accValues = Array(3) { i -> event.values[i].toDouble() }
            sensorDataQueue.put(accValues)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        stopLocationUpdates()
        stopSensorUpdates()
        super.onDestroy()
        executorService.shutdownNow()
    }
    private fun getNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tracking")
            .setContentText("Tracking the location in the background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // For Android O and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Used for the location tracking service"
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Location Tracking Channel"
            val descriptionText = "Used for the location tracking service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun classifyActivityWithWeka(featureVector: ArrayList<Double>): String {

        val wekaCompatibleVector = featureVector.map { it as Object }.toTypedArray()

        // Classify the activity
        val activityIndex = WekaClassifier.classify(wekaCompatibleVector)

        //Based on provided features.arff/datacollector app
        //Do not have data for other activities
        val classifiedActivity = when (activityIndex) {
            0.0 -> "Standing"
            1.0 -> "Walking"
            2.0 -> "Running"
            else -> "Unknown"
        }

        //Post the classified activity to LiveData
        activityTypeUpdates.postValue(classifiedActivity)

        return classifiedActivity

    }

}