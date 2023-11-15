package com.example.derek_huang_myruns1

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat

class TrackingService : Service() {

    private lateinit var locationManager: LocationManager
    private val locationListener: LocationListener = LocationListener { location ->
        // Update location data to LiveData
        locationUpdates.postValue(location)
    }

    companion object {
        val locationUpdates: MutableLiveData<Location> = MutableLiveData()
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
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10f, locationListener)
        }
    }

    private fun stopLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, getNotification())
        startLocationUpdates()
        return START_STICKY
    }
    override fun onDestroy() {
        stopLocationUpdates()
        super.onDestroy()
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
}