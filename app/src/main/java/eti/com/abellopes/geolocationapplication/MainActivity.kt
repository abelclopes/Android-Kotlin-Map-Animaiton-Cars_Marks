package eti.com.abellopes.geolocationapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() , OnMapReadyCallback {

    private val LOCATION_REQUEST_CODE = 101
    private var mGoogleMap: GoogleMap? = null

    private var getRemoteDrivers = MutableLiveData<MutableList<Drivers>>().apply { value = mutableListOf() }

    private var markerList: ArrayList<Marker> = arrayListOf()
    val path: MutableList<List<LatLng>> = arrayListOf()

    var latitude: Double = -30.026290293241992
    var longitude: Double = -51.22640490531922

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        getData()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap!!

        mGoogleMap!!.uiSettings.isRotateGesturesEnabled = false
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = false
        mGoogleMap!!.uiSettings.setAllGesturesEnabled(false)

        mGoogleMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                return true
            }
        })
        locationsOnMap()

        val myLocation = LatLng(latitude, longitude)
        //mGoogleMap!!.addMarker(MarkerOptions().position(myLocation).title("My Location"))
        mGoogleMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 14.5f))



        for (i in 0 until path.size) {
            onLocationChanged(path[i], i)
        }
    }

    private fun onLocationChanged(location: List<LatLng>, index: Int = 0) {
        updateRiderPosition(location[index])
    }

    private fun updateRiderPosition(location: LatLng, update: Boolean = false) {
//        // this if remove all previous markers
        if (update) {
            for (marker in markerList) {
                marker.remove()
            }
        }
        val cMarker = mGoogleMap!!.addMarker(
            MarkerOptions().position(location).icon(
                MapsUtil.bitmapDescriptorFromVector(
                    applicationContext,
                    R.drawable.ic_car_map_ativo
                )
            )
        )
        markerList.add(cMarker)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }


    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }



    fun locationsOnMap(){
        path.addAll(
            listOf(
                mutableListOf(
                    LatLng(-30.026290293241992,-51.22640490531922),
                    LatLng(-30.026373894045893, -51.22586846351624),
                    LatLng(-30.025742241781554, -51.22496724128724),
                    LatLng(-30.025612195227527, -51.2259328365326),
                    LatLng(-30.026271715276, -51.227520704269416),
                    LatLng(-30.026931230936835, -51.230084896087654),
                    LatLng(-30.027804385766014,-51.23184442520142),
                    LatLng(-30.028928329064602, -51.23411893844605),
                    LatLng(-30.030284475087335, -51.23692989349366),
                    LatLng(-30.033005999821437, -51.2398910522461)
                )
            )
        )

        path.addAll(
            listOf(
                mutableListOf(
                    LatLng(-30.028240960296493,-51.22996151447297),
                    LatLng(-30.028008740040917, -51.22938752174378),
                    LatLng(-30.02784154112006, -51.2290120124817),
                    LatLng(-30.027618608786877, -51.22836828231812),
                    LatLng(-30.026271715276, -51.227520704269416),
                    LatLng(-30.027228475997447,-51.22850239276887),
                    LatLng(-30.02707520912436,-51.22839510440827),
                    LatLng(-30.02689407524158,-51.22760653495789)

                )
            )
        )

        path.addAll(
            listOf(
                mutableListOf(
                    LatLng(-30.030925386956547,-51.22519254684449),
                    LatLng(-30.030851078546416, -51.22639417648316),
                    LatLng(-30.030813924320473, -51.227295398712165),
                    LatLng(-30.030813924320473, -51.22774600982666),
                    LatLng(-30.031074003609675, -51.227896213531494),
                    LatLng(-30.031779929668794,-51.227896213531494),
                    LatLng(-30.032392966641055,-51.227896213531494),
                    LatLng(-30.0332289200378,-51.2280035018921)

                )
            )
        )
    }


    fun getData() {
        val retrofitClient = NetworkUtils
            .getRetrofitInstance("https://private-fdb8bc-sohaferiageolocation.apiary-mock.com")

        val endpoint = retrofitClient.create(Endpoint::class.java)
        val callback = endpoint.getDrivers()

        callback.enqueue(object : Callback<DriversResponse> {
            override fun onResponse(call: Call<DriversResponse>, response: Response<DriversResponse>) {
                //getRemoteDrivers.value!!.addAll(response.body()!!.drivers!!)
                Log.d("TAG", "REQUEST ${response.body()!!.drivers}")
            }

            override fun onFailure(call: Call<DriversResponse>, t: Throwable) {

                Log.d("TAG", "onFailure ${t.message}")
            }

        })

    }
}
