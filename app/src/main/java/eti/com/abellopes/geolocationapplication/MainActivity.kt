package eti.com.abellopes.geolocationapplication

import android.animation.ValueAnimator
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import eti.com.abellopes.geolocationapplication.MapsUtil.Companion.getBearing
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLngBounds




class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val LOCATION_REQUEST_CODE = 101
    private val ANIMATION_TIME_PER_ROUTE: Long = 3000
    private var mGoogleMap: GoogleMap? = null

    private var getRemoteDrivers = MutableLiveData<MutableList<Drivers>>().apply { value = mutableListOf() }

    private var markerList: ArrayList<Marker> = arrayListOf()
    private var polyLineList: List<LatLng> = listOf()
    private var polylineOptions: PolylineOptions? = null
    private var greyPolyLine: Polyline? = null
    private var carMarker: Marker? = null
    var hashMap = HashMap<String, Marker>()

    lateinit var googleMapHelper: GoogleMapHelper

    var latitude: Double = -30.02808769498885
    var longitude: Double = -51.22985422611237


    private var startPosition: LatLng? = null
    private var endPosition: LatLng? = null

    private var isFirstPosition: Boolean = true


    var coordsHashMap: HashMap<String, List<LatLng>> = hashMapOf()


    private var startTimeInMilliSeconds = 0L


    private var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        if(isRouteTrackingOn()){
            startTimeInMilliSeconds = System.currentTimeMillis();
            Log.d("TAG", "Current time " + startTimeInMilliSeconds);
            Log.d("TAG", "Service is running");
        }
    }

    private fun isRouteTrackingOn(): Boolean {
        Log.d("TAG", "SERVICE STATE ")
        return true
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mGoogleMap = googleMap!!

        setGoogleMapSettings()

        val myLocation = LatLng(latitude, longitude)
        updateRiderPosition(myLocation)

        getData()
    }


    private fun setGoogleMapSettings() {
        mGoogleMap!!.uiSettings.isRotateGesturesEnabled = false
        mGoogleMap!!.uiSettings.isZoomControlsEnabled = false
        mGoogleMap!!.uiSettings.setAllGesturesEnabled(false)

        mGoogleMap!!.setMinZoomPreference(6.0f)
        mGoogleMap!!.setMaxZoomPreference(16.0f)

        mGoogleMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(p0: Marker?): Boolean {
                return true
            }
        })
    }

    private fun onLocationChanged(location: List<LatLng>, index: Int = 0) {
        updateRiderPosition(location[index])
    }

    private fun creteStartMarker(location: LatLng, id: String){
        val cMarker = mGoogleMap!!.addMarker(
            MarkerOptions().position(location).icon(
                MapsUtil.bitmapDescriptorFromVector(
                    applicationContext,
                    R.drawable.ic_car_map_ativo
                )
            )
        )
        cMarker.title = id
        hashMap[id] = cMarker
    }

    private fun updateRiderPosition(location: LatLng, update: Boolean = false) {
        if (update) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hashMap.forEach { t, u ->
                    u.remove()
                }
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
        mGoogleMap!!.animateCamera(
            CameraUpdateFactory
                .newCameraPosition
                    (
                    CameraPosition.Builder()
                        .target(location)
                        .zoom(17.5f)
                        .build()
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


    fun createMarksOnMaps(){
        if (getRemoteDrivers.value!!.size > 0) {
            getRemoteDrivers.value!!.forEach { driver ->
                var coords: MutableList<LatLng> = mutableListOf()
                next = 0
                index = 0

                startPosition = null

                creteStartMarker(LatLng(driver.location.current[0], driver.location.current[1]), driver.id)


                driver.location.history.forEach { (latitude, longitude) ->
                    coords.addAll(mutableListOf(LatLng(latitude,longitude)))
                }

                val builder = LatLngBounds.Builder()
                builder.include(LatLng(latitude,longitude))
                val bounds = builder.build()
                val mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2)
                mGoogleMap!!.animateCamera(mCameraUpdate)
                coordsHashMap.put(driver.id, coords)
            }
            calculateZoomCars()

            handler.postDelayed({ this
                coordsHashMap.forEach { (idDriver, coords) ->
                    handler.postDelayed({
                        this
                        showOrAnimateMarker(coords, start = LatLng(latitude,longitude), idDriver = idDriver)
                    }, 1000)
                }
            }, 3000)
        }
    }


    private fun calculateZoomCars(){

        val builder = LatLngBounds.Builder()
        for(marker in hashMap) {
            builder.include(marker.value.position)
        }
        val bounds = builder.build()

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = (width * 0.10).toInt() // offset from edges of the map 10% of screen

        val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

        mGoogleMap!!.animateCamera(cu)
    }

    private var index: Int = 0
    private var next: Int = 0

    private fun showOrAnimateMarker(coords: List<LatLng>, start: LatLng , idDriver : String?) {
        if (index < (coords.size - 1)) {
            index++
            next = index + 1
        } else {
            index = -1
            next = 1
        }

        if (index < (coords.size - 1)) {
           // startPosition = coords[index]
            startPosition = hashMap[idDriver]!!.position
            endPosition = coords[next]
        }
        startBikeAnimation(start = startPosition!!, end = endPosition!!, carDriver = idDriver!!)
    }

    private fun getData() {
        val retrofitClient = NetworkUtils
            .getRetrofitInstance("https://private-fdb8bc-sohaferiageolocation.apiary-mock.com")

        val endpoint = retrofitClient.create(ApiInterface::class.java)
        val callback = endpoint.getDrivers()

        callback.enqueue(object : Callback<DriversResponse> {
            override fun onResponse(call: Call<DriversResponse>, response: Response<DriversResponse>) {
                getRemoteDrivers.value!!.addAll(response.body()!!.drivers!!)
                Log.d("TAG", "REQUEST ${response.body()}")
                createMarksOnMaps()
            }

            override fun onFailure(call: Call<DriversResponse>, t: Throwable) {

                Log.d("TAG", "onFailure ${t.message}")
            }

        })

    }

    private fun startBikeAnimation(start: LatLng, end: LatLng, carDriver: String) {

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
                var marker = hashMap[carDriver]
                marker!!.title = carDriver
                marker.position = newPos
                marker.setAnchor(0.5F, 0.5F)
                marker.rotation = getBearing(start, end)

                // todo : Shihab > i can delay here
                mGoogleMap!!.animateCamera(
                    CameraUpdateFactory
                        .newCameraPosition
                            (
                            CameraPosition.Builder()
                                .target(newPos)
                                .zoom(17.5f)
                                .build()
                        )
                )

                startPosition = marker.position

            }

        })
        valueAnimator.start()

        handler.postDelayed({ this }, 10000)
    }

}
