package eti.com.abellopes.geolocationapplication


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.GoogleMap


class MapsUtil {

    companion object {

        fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
            val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
            vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }


        fun decodePoly(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val p = LatLng(
                    lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5
                )
                poly.add(p)
            }

            return poly
        }

        // // TODO: 1/22/2017 please check
        fun fixZoomProblem(source: LatLng, distance: LatLng, points: List<LatLng>, googleMap: GoogleMap) {

            val bc = LatLngBounds.Builder()

            bc.include(source)
            bc.include(distance)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), 50))
        }

        fun fitZoomWithScreen(source: LatLng, distance: LatLng, googleMap: GoogleMap, context: Context) {

            val builder = LatLngBounds.Builder()

            //the include method will calculate the min and max bound.
            builder.include(source)
            builder.include(distance)

            //        builder.include(marker3.getPosition());
            //        builder.include(marker4.getPosition());
            val bounds = builder.build()

            val width = context.resources.displayMetrics.widthPixels
            val height = context.resources.displayMetrics.heightPixels - 250
            val padding = (width * 0.20).toInt()
            // offset from edges of the map 12% of screen

            val cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)

            googleMap.animateCamera(cu)

        }

        // https://stackoverflow.com/questions/24812483/how-to-create-bounds-of-a-android-polyline-in-order-to-fit-the-screen

        fun moveToBounds(context: Context, p: Polyline): CameraUpdate {

            val builder = LatLngBounds.Builder()
            val arr = p.points
            for (i in arr.indices) {
                builder.include(arr[i])
            }
            val bounds = builder.build()
            val padding = 100 // offset from edges of the map in pixels

            val width = context.resources.displayMetrics.widthPixels
            val height = context.resources.displayMetrics.heightPixels / 2

            //        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);


            return CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding)
            //        mMap.animateCamera(cu);
        }

        fun getBearing(begin: LatLng, end: LatLng): Float {
            val lat = Math.abs(begin.latitude - end.latitude)
            val lng = Math.abs(begin.longitude - end.longitude)

            if (begin.latitude < end.latitude && begin.longitude < end.longitude)
                return Math.toDegrees(Math.atan(lng / lat)).toFloat()
            else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
                return (90 - Math.toDegrees(Math.atan(lng / lat)) + 90).toFloat()
            else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
                return (Math.toDegrees(Math.atan(lng / lat)) + 180).toFloat()
            else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
                return (90 - Math.toDegrees(Math.atan(lng / lat)) + 270).toFloat()
            return -1f
        }

//        private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
//            val background = ContextCompat.getDrawable(context, R.drawable.ic_car_map_ativo)
//            background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
//            val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
//            vectorDrawable!!.setBounds(40, 20, vectorDrawable.intrinsicWidth + 40, vectorDrawable.intrinsicHeight + 20)
//            val bitmap =
//                Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            background.draw(canvas)
//            vectorDrawable.draw(canvas)
//            return BitmapDescriptorFactory.fromBitmap(bitmap)
//        }

    }
}