package com.example.restaurantreview.main.view

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.example.restaurantreview.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddRestaurantMaps.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRestaurantMaps : Fragment(), OnMapReadyCallback {
    private lateinit var mapView : MapView
    private lateinit var mMap: GoogleMap
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_add_restaurant_maps, container, false)
        // Inflate the layout for this fragment
        if (context?.let { ContextCompat.checkSelfPermission(it,android.Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mapView = rootView.findViewById(R.id.map1)!! // 저장한 기본값에 구글맵 추가
            mapView.onCreate(savedInstanceState)
            mapView.getMapAsync(this)
        }

        return rootView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddRestaurantMaps.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddRestaurantMaps().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onMapReady(map: GoogleMap) { // 반월당 뜨면 성공

        mMap = map
        // Add a marker in Sydney and move the camera
        val daegu = LatLng(35.86667, 128.5933)
        mMap.addMarker(MarkerOptions().position(daegu).title("Marker in daegu"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(daegu, 16f))
        setMarker()
        mMap.setOnCameraChangeListener { setMarker() }
    }

    // 구글맵 생명주기 만들어주기
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    // 마커 관련
    private fun setMarker(){

        // 마커 설정하는 함수
        mMap?.let {
            it.clear() // 지도에 있는 마커를 먼저 삭제
            val markerOptions = MarkerOptions()
            markerOptions.position(it.cameraPosition.target) // 마커의 위치 설정
            val marker = it.addMarker(markerOptions) // 지도에 마커를추가하고, 마커 객체를 리턴
            it.setOnCameraMoveListener {
                marker?.let { marker ->
                    marker.setPosition(it.cameraPosition.target)
                }
            }
            val mActivity = activity as AddRestaurantDataActivity
            mActivity.receiveMarker( markerOptions.position.latitude.toDouble(), markerOptions.position.longitude.toDouble())


        }
    }
}