package eti.com.abellopes.geolocationapplication

import com.google.gson.annotations.SerializedName

data class DriversResponse(
    val success: Boolean,
    val total: Int,

    @SerializedName("data")
    val drivers: List<Drivers>
)