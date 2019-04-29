package eti.com.abellopes.geolocationapplication

import com.google.gson.annotations.SerializedName

data class DriversResponse(
    @SerializedName("tracking")
    val drivers: List<Drivers>
)

