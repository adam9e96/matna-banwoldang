package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.DialogMapsBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivity
import com.google.android.gms.maps.MapView

interface DetailMapsDialogInterface {
    fun onClickMapYesButton(id: Int,lat: Double,long: Double)
}
class DetailMapsDialog(mapsDialogInterface: RestaurantDetailActivity, lat: Double, long: Double, id: Int): DialogFragment() {
    private var mapsDialogInterface: DetailMapsDialogInterface? = null
    private var _binding: DialogMapsBinding?= null
    private val binding get() = _binding!!
    private var id: Int = 0
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private lateinit var mapView: MapView
    init {
        this.mapsDialogInterface = mapsDialogInterface
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
            mapView.getMapAsync(activity as RestaurantDetailActivity)
        }
        // 취소 버튼이 없는 다이얼로그는 id를 -1로 넘겨줌
        if (id == -1) {
            // 취소 버튼을 보이지 않게 처리
            binding.dialogNoBtn.visibility = View.GONE
        }

        binding.dialogYesBtn.setOnClickListener {
            mapsDialogInterface?.onClickMapYesButton(id,lat,long)
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