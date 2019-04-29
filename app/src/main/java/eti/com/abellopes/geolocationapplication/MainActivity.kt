package eti.com.abellopes.geolocationapplication

import android.animation.ValueAnimator
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.JointType.ROUND
import eti.com.abellopes.geolocationapplication.MapsUtil.Companion.getBearing
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_REQUEST_CODE = 101
    private val ANIMATION_TIME_PER_ROUTE: Long = 3000
    private var mGoogleMap: GoogleMap? = null

    private var getRemoteDrivers = MutableLiveData<MutableList<Drivers>>().apply { value = mutableListOf() }

    private var markerList: ArrayList<Marker> = arrayListOf()
    private var polyLineList: List<LatLng> = listOf()
    private var polylineOptions: PolylineOptions? = null
    private var greyPolyLine: Polyline? = null
    private lateinit var carMarker: Marker

    var latitude: Double = -30.02808769498885
    var longitude: Double = -51.22985422611237


    private var startPosition: LatLng? = null
    private var endPosition: LatLng? = null

    private var isFirstPosition: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap!!

        mGoogleMap!!.uiSettings.isRotateGesturesEnabled = false
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = false
        mGoogleMap!!.uiSettings.setAllGesturesEnabled(false)
        mGoogleMap!!.setMinZoomPreference(6.0f)
        mGoogleMap!!.setMaxZoomPreference(15.0f)
        mGoogleMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                return true
            }
        })

        val myLocation = LatLng(latitude, longitude)


        getData()
        mGoogleMap!!.addMarker(MarkerOptions().position(myLocation))
        updateRiderPosition(myLocation)
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


    fun CreatePolyLineOnly() {

        mGoogleMap!!.clear()
        if (getRemoteDrivers.value!!.size > 0) {
            getRemoteDrivers.value!!.forEach { drivers ->

                var coords: MutableList<LatLng> = mutableListOf()
                drivers.location.history.forEach { (latitude, longitude) ->
                    coords.addAll(mutableListOf(LatLng(latitude,longitude)))
                    val builder = LatLngBounds.Builder()
                    builder.include(LatLng(latitude,longitude))
                    val bounds = builder.build()
                    val mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2)
                    mGoogleMap!!.animateCamera(mCameraUpdate)

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.RED)
                    polylineOptions!!.width(5F)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(ROUND)
                    polylineOptions!!.addAll(coords)
                    greyPolyLine = mGoogleMap!!.addPolyline(polylineOptions)

                }
                onLocationChanged(mutableListOf(LatLng(drivers.location.current[0], drivers.location.current[1])))
            }
        }
    }

    private fun startBikeAnimation(start: LatLng, end: LatLng) {

        Log.i("TAG", "startBikeAnimation called...")

        var valueAnimator = ValueAnimator.ofFloat(0F, 1F)
        valueAnimator.duration = ANIMATION_TIME_PER_ROUTE
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
                //LogMe.i(TAG, "Car Animation Started...")
                var v = valueAnimator.animatedFraction
                var lng = v * end.longitude + (1 - v) * start.longitude
                var lat = v * end.latitude + (1 - v) * start.latitude

                var newPos = LatLng(lat, lng)
                carMarker!!.position = newPos
                carMarker!!.setAnchor(0.5F, 0.5F)
                carMarker!!.rotation = getBearing(start, end)

                // todo : Shihab > i can delay here
                mGoogleMap!!.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition
                            (
                            CameraPosition.Builder()
                                .target(newPos)
                                .zoom(15.5f)
                                .build()
                        )
                )

                startPosition = carMarker!!.position

            }

        })
        valueAnimator.start()
    }

    fun getData() {
        val retrofitClient = NetworkUtils
            .getRetrofitInstance("https://private-fdb8bc-sohaferiageolocation.apiary-mock.com")

        val endpoint = retrofitClient.create(ApiInterface::class.java)
        val callback = endpoint.getDrivers()

        callback.enqueue(object : Callback<DriversResponse> {
            override fun onResponse(call: Call<DriversResponse>, response: Response<DriversResponse>) {
                getRemoteDrivers.value!!.addAll(response.body()!!.drivers!!)
                Log.d("TAG", "REQUEST ${response.body()}")
                CreatePolyLineOnly()
            }

            override fun onFailure(call: Call<DriversResponse>, t: Throwable) {

                Log.d("TAG", "onFailure ${t.message}")
            }

        })

    }

}
