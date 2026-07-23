package com.example.restaurantreview.main.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.elvishew.xlog.XLog
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityRestaurantDetailBinding
import com.example.restaurantreview.main.dialog.ConfirmDialog
import com.example.restaurantreview.main.dialog.ConfirmDialogInterface
import com.example.restaurantreview.main.dialog.DetailMapsDialog
import com.example.restaurantreview.main.dialog.DetailMapsDialogInterface
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

/**
 * 특정 레스토랑(맛집)의 세부 정보를 표시하는 액티비티
 */
class RestaurantDetailActivity : AppCompatActivity(), ConfirmDialogInterface, OnMapReadyCallback,
    DetailMapsDialogInterface {
    private lateinit var binding: ActivityRestaurantDetailBinding
    private lateinit var restaurantManager: FirebaseManager
    private lateinit var updateRestaurantLauncher: ActivityResultLauncher<Intent>
    private lateinit var shapeableImageView: ShapeableImageView
    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private lateinit var restaurantId: String
    private lateinit var imageUrl: String
    private lateinit var bnv: BottomNavigationView
    private var firebaseManager: FirebaseManager = FirebaseManager(this)

    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var isZoomed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantManager = FirebaseManager(this) // RestaurantManager 초기화

        val restaurant: Restaurant? = getRestaurantFromIntent()

        // 선택된 맛집 데이터로 UI 업데이트 및 id(키값), url 값은 추후 사용을 위해 저장
        restaurant?.let {
            restaurantId = it.id
            imageUrl = it.imageURl
            updateUI(it)
            Log.d("테스트임", "$restaurantId , $imageUrl")
        }

        mapView = binding.DetailMap
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setupButtons(restaurant)
        setupActivityResultLauncher()

        // 하단 내비게이션바 관련
        setupBottomNavigation()

        shapeableImageView = binding.detailRsImage
        shapeableImageView.setOnClickListener {
            toggleZoom()
        }
    } // onCreate() END

    /**
     * 이미지 확대/축소 하는 함수
     */
    private fun toggleZoom() {
        val scale = if (isZoomed) 1f else 2f
        shapeableImageView.animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(300)
            .withEndAction { isZoomed = !isZoomed }
            .start()
    }

    /**
     * 네비게이션바 설정 함수
     */
    private fun setupBottomNavigation() {
        bnv = binding.bnvMain

        bnv.menu.getItem(0).isChecked = true

        bnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naviHome -> {// 메인페이지
                    goToMainActivity()
                    true
                }

                R.id.naviSearch -> {// 조회페이지
                    goToMapsActivity()
                    true
                }

                R.id.naviFavorite -> {// 좋아요페이지
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

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
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

    private fun goToAddRestaurantDataActivity() {
        val intent = Intent(this, AddRestaurantDataActivity::class.java)
        startActivity(intent)
    }

    /**
     * 액티비티 결과를 처리하기 위해 ActivityResultLauncher를 설정하는 함수
     */
    private fun setupActivityResultLauncher() {
        updateRestaurantLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedRestaurantId = result.data?.getStringExtra("restaurantId")
                val updatedLat = result.data?.getDoubleExtra("latitude", 0.0)
                val updatedLong = result.data?.getDoubleExtra("longitude", 0.0)
                val updatedImageUrl = result.data?.getStringExtra("imageUrl")

                updatedRestaurantId?.let { id ->
                    restaurantManager.getRestaurantById(id) { restaurant ->
                        restaurant?.let {
                            updateUI(it)
                            updatedLat?.let { lat ->
                                updatedLong?.let { long ->
                                    updateMap(lat, long)
                                }
                            }
                            updatedImageUrl?.let { imageUrl ->
                                Picasso.get().load(imageUrl).into(binding.detailRsImage)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 지도 업데이트 함수
     * @param lat 위도
     * @param long 경도
     */
    private fun updateMap(lat: Double, long: Double) {
        mMap.clear()  // 기존 마커 삭제
        val location = LatLng(lat, long)
        mMap.addMarker(
            MarkerOptions().position(location).title(binding.restaurantName.text.toString())
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
    }

    /**
     * 버튼 클릭 리스너 설정 함수
     * @param restaurant 맛집 객체
     */
    private fun setupButtons(restaurant: Restaurant?) {
        /**
         * 뒤로 가기 버튼 클릭 리스너 설정
         * MainActivity로 이동하고 현재 액티비티를 종료
         */
        binding.topAppBar.setNavigationOnClickListener {
            navigateToMainActivity()
        }
        /**
         * 수정 버튼 클릭 리스너 설정
         */
        binding.detailModBtn.setOnClickListener {
            restaurant?.let {
                navigateToModActivity(it)
            }
        }
        /**
         * 삭제 버튼 클릭 리스너 설정
         */
        binding.detailDeleteBtn.setOnClickListener {
            restaurant?.let {
                delete()
            }
        }
    }

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    /**
     * 수정 페이지로 이동하는 함수
     * @param restaurant 레스토랑 객체
     */
    private fun navigateToModActivity(restaurant: Restaurant) {
        val intent = Intent(this, RestaurantDetailActivityMod::class.java).apply {
            putExtra("restaurant", restaurant)
            putExtra("restaurantId", restaurant.id)
        }
        updateRestaurantLauncher.launch(intent)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // API 레벨에 따라 적절한 메서드 사용
    private fun getRestaurantFromIntent(): Restaurant? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("restaurant", Restaurant::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("restaurant")
        }
    }

    private fun updateUI(restaurant: Restaurant) {
        binding.restaurantName.text = restaurant.store
        binding.restaurantMenu.text = restaurant.menu
        binding.restaurantReview.text = restaurant.review
        binding.restaurantRatingScore.rating = restaurant.rating
        lat = restaurant.latitude
        long = restaurant.longitude

        // Update Chip text
        binding.restaurantTagsGroup.removeAllViews() // 기존 태그 삭제

        restaurant.tags.forEach { tag ->
            val chip = LayoutInflater.from(this)
                .inflate(R.layout.item_chip_detail, binding.restaurantTagsGroup, false) as Chip
            chip.text = tag
            binding.restaurantTagsGroup.addView(chip)
        }

        binding.restaurantBusinessDays.text = restaurant.businessDays
        binding.restaurantBusinessHoursStart.text = restaurant.businessHours.substring(
            0,
            restaurant.businessHours.indexOf("-")
        )
        binding.restaurantBusinessHoursEnd.text =
            restaurant.businessHours.substring(restaurant.businessHours.indexOf("-") + 1)

        val empty = "휴게 시간 없음"
        if (restaurant.breakTime.isNullOrEmpty()) {
            binding.restaurantBreakTimeStart.text = empty
            binding.restaurantBreakTimeEnd.visibility = GONE
        } else {
            binding.restaurantBreakTimeStart.text =
                restaurant.breakTime.let {
                    restaurant.breakTime.substring(
                        0,
                        restaurant.breakTime.indexOf("-")
                    )
                }
            binding.restaurantBreakTimeEnd.text =
                restaurant.breakTime.let {
                    restaurant.breakTime.substring(
                        restaurant.breakTime.indexOf("-") + 1
                    )
                }
        }
        binding.restaurantCategory.text = restaurant.category
        // 리뷰 날짜 형식을 변환
        val reviewDate = restaurant.reviewDate.split("-")
        val formattedDate = "${reviewDate[0]}년 ${reviewDate[1]}월 ${reviewDate[2]}일"
        binding.restaurantReviewDate.text = formattedDate
        binding.restaurantReviewDate.text = restaurant.reviewDate
        binding.restaurantBookmark.isChecked = restaurant.bookmark

        // 이미지 로드
        Picasso.get().load(restaurant.imageURl).into(binding.detailRsImage)
    }

    override fun onClickYesButton(id: Int) {
        if (id == 1) {
            restaurantId.let { key ->
                imageUrl.let { url ->
                    restaurantManager.deleteImageByUrl(url) { isSuccessDelImg ->
                        // 이미지 삭제 성공 여부에 관계없이 실행
                        restaurantManager.deleteRestaurantById(key) { isSuccessDelRestaurant ->
                            if (isSuccessDelRestaurant) {
                                navigateToMainActivity()
                            }
                            if (isSuccessDelImg && isSuccessDelRestaurant) {
                                Toast.makeText(this, "이미지 및 맛집 삭제 성공", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 삭제 확인 다이얼로그를 표시하는 함수
     */
    private fun delete() {
        val title = "정말 계정을 삭제하시겠어요?"
        val content = "지금까지의 정보가 모두 사라집니다."

        val dialog = ConfirmDialog(this@RestaurantDetailActivity, title, content, "확인", 1)
        dialog.isCancelable = false

        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    /**
     * 지도 준비 완료 시 호출되는 함수
     * @param map GoogleMap 객체
     */
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        val daegu = LatLng(lat, long)
        mMap.addMarker(
            MarkerOptions().position(daegu).title(binding.restaurantName.text.toString())
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(daegu, 16f))
        mMap.setOnMapClickListener {
            Log.i("latlong, ", "$lat, $long")
            showDetailMapsDialog(lat, long)
        }
    }

    // 구글맵 생명주기 관리
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

    /**
     * 지도 다이얼로그를 표시하는 함수
     */
    private fun showDetailMapsDialog(lat: Double, long: Double) {
        val dialog = DetailMapsDialog(this@RestaurantDetailActivity, lat, long, -1)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ModMapsDialog")
    }

    override fun onClickMapYesButton(id: Int, lat: Double, long: Double) {
        Log.i("latlong, ", "$lat, $long")
    }
}
