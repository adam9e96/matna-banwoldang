package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.DialogMapsBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod
import com.google.android.gms.maps.MapView

interface ModMapsDialogInterface {
    fun onClickMapYesButton(id: Int,lat: Double,long: Double)
}
class ModMapsDialog(mapDialogInterface: RestaurantDetailActivityMod, lat: Double, long: Double, id: Int): DialogFragment(){

    private var mMapDialogInterface: ModMapsDialogInterface? = null
    private var _binding: DialogMapsBinding?= null
    private val binding get() = _binding!!
    private var id: Int = 0
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private lateinit var mapView: MapView
    init {
        this.mMapDialogInterface = mapDialogInterface
        this.id = id
        this.lat = lat
        this.long = long
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMapsBinding.inflate(inflater, container, false)
        val view = binding.root
        mapView = view.findViewById(R.id.dialog_map)!! // 저장한 기본값에 구글맵 추가
        mapView.onCreate(savedInstanceState).also {
            mapView.getMapAsync(activity as RestaurantDetailActivityMod)
        }
        binding.dialogYesBtn.setOnClickListener {
            mMapDialogInterface?.onClickMapYesButton(id,lat,long)
            dismiss()
        }
        binding.dialogNoBtn.setOnClickListener {
            dismiss()
        }
        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
}