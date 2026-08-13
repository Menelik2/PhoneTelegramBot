package com.example.telegrambot.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper

object LocationHelper {
    @SuppressLint("MissingPermission")
    fun getLocation(context: Context, onResult: (String) -> Unit) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            
            if (!isGpsEnabled && !isNetworkEnabled) {
                onResult("❌ Location services are disabled on the device.")
                return
            }
            
            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            
            // Try last known location first to return immediately if possible
            val lastKnown = locationManager.getLastKnownLocation(provider)
            if (lastKnown != null) {
                onResult("📍 *Location (Last Known)*\nLatitude: `${lastKnown.latitude}`\nLongitude: `${lastKnown.longitude}`\nMap: [Google Maps](https://maps.google.com/?q=${lastKnown.latitude},${lastKnown.longitude})")
                return
            }
            
            // Request single update on main thread looper
            locationManager.requestSingleUpdate(provider, object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    onResult("📍 *Current Location*\nLatitude: `${location.latitude}`\nLongitude: `${location.longitude}`\nMap: [Google Maps](https://maps.google.com/?q=${location.latitude},${location.longitude})")
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }, Looper.getMainLooper())
            
        } catch (e: Exception) {
            e.printStackTrace()
            onResult("❌ Failed to get location: ${e.message}")
        }
    }
}
