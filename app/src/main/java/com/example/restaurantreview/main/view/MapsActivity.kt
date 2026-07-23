package com.example.restaurantreview.main.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.elvishew.xlog.XLog
import com.example.restaurantreview.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.restaurantreview.databinding.ActivityMapsBinding
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso


/**
 * 레스토랑 위치를 지도에 표시하는 액티비티
 *
 * 주요 기능:
 * - Firebase에서 레스토랑 데이터를 로드하여 지도에 마커로 표시
 * - 마커 클릭 시 레스토랑 세부 정보 표시
 * - 하단 내비게이션을 통한 페이지 이동
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var restaurantList: MutableList<Restaurant> = mutableListOf()
    private lateinit var bnv: BottomNavigationView
    private var firebaseManager: FirebaseManager = FirebaseManager(this)
    private lateinit var restaurantManager: FirebaseManager
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.cardView.visibility = View.GONE
        setupBottomNavigation()

        // SupportMapFragment 초기화 및 콜백 등록
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    } // onCreate() END

    /**
     * 지도가 준비되었을 때 호출되는 콜백 메서드
     * @param googleMap 준비된 GoogleMap 객체
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mapReady()
    }

    /**
     * 지도에 레스토랑 데이터를 로드하고 마커를 추가하는 함수
     */
    @SuppressLint("PotentialBehaviorOverride")
    private fun mapReady() {
        restaurantManager = FirebaseManager(this) // RestaurantManager 초기화
        restaurantManager.loadRestaurantData { restaurants ->
            restaurantList = restaurants.toMutableList()
            XLog.d("restaurantList -  $restaurantList")
            val builder = LatLngBounds.Builder()

            for (restaurant in restaurantList) {
                val marker: Marker? = mMap.addMarker(
                    MarkerOptions().position(LatLng(restaurant.latitude, restaurant.longitude))
                        .title(restaurant.store)
                )
                builder.include(LatLng(restaurant.latitude, restaurant.longitude))
                Log.d("mapReady", restaurant.store)

                if (marker != null) {
                    marker.tag = restaurant.id to restaurant.imageURl
                    marker.title = restaurant.store
                    marker.snippet = restaurant.reviewDate
                    marker.zIndex = restaurant.rating
                }
            }

            mMap.setOnMarkerClickListener { marker ->
                binding.run {
                    cardView.visibility = View.VISIBLE
                    rsName.text = marker.title
                    rsRating.text = marker.zIndex.toString()
                    rsReviewDate.text = marker.snippet
                    val imageUrl = (marker.tag as Pair<String, String>).second
                    Picasso.get()
                        .load(imageUrl)
                        .resize(300, 300)
                        .centerInside()
                        .into(rsImage)

                    cardView.setOnClickListener {
                        Log.d(
                            "intent",
                            (marker.tag as Pair<String, String>).first.replaceAfter("(", "")
                        )
                        firebaseManager.getRestaurantById(
                            (marker.tag as Pair<String, String>).first.replaceAfter(
                                "(",
                                ""
                            )
                        ) { restaurant ->
                            val intent = Intent(
                                this@MapsActivity,
                                RestaurantDetailActivity::class.java
                            ).apply {
                                putExtra("restaurant", restaurant)
                            }
                            startActivity(intent)

                        }

                    }
                }
                false
            }

            mMap.setOnMapClickListener {
                binding.cardView.visibility = View.GONE
            }

            val bounds = builder.build()
            val cu = CameraUpdateFactory.newLatLngBounds(bounds, 100)
            mMap.moveCamera(cu)
        }
    }

    private fun setupBottomNavigation() {
        bnv = binding.bnvMain

        bnv.menu.getItem(1).isChecked = true

        bnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naviHome -> {// 메인페이지
                    gotoMainActivity()
                    true
                }

                R.id.naviSearch -> {// 조회페이지
                    goToMapsActivity()
                    true
                }

                R.id.naviFavorite -> {// 북마크페이지
                    goToBookmarkActivity()
                    true
                }

                R.id.naviAdd -> {// 등록 페이지
                    goToAddRestaurantDataActivity()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun gotoMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    // 등록페이지로 보내는 인텐트
    private fun goToAddRestaurantDataActivity() {
        val intent = Intent(this, AddRestaurantDataActivity::class.java)
        startActivity(intent)
    }

    private fun goToBookmarkActivity() {
        firebaseManager.loadBookmarkedRestaurants { restaurantList ->
            val intent = Intent(this, BookmarkActivity::class.java).apply {
                putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            }
            startActivity(intent)
        }
    }
}