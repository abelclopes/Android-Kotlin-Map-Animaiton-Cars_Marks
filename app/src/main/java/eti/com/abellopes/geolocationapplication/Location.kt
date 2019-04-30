package eti.com.abellopes.geolocationapplication

import android.util.ArrayMap

data class Location (
    val current: List<Double>,
    val history: List<List<Double>>,
    val latitude: Double,
    val longitude: Double
)