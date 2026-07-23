package com.example.restaurantreview.main.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityBanner1Binding
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Statistics
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Banner1Activity : AppCompatActivity() {
    private lateinit var binding: ActivityBanner1Binding
    private lateinit var bnv: BottomNavigationView
    private var firebaseManager: FirebaseManager = FirebaseManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBanner1Binding.inflate(layoutInflater)

        setContentView(binding.root)

        setupBottomNavigation()

        val statisticsManager = StatisticsManager()

        statisticsManager.calculateStatistics { statistics ->
            runOnUiThread {
                findViewById<TextView>(R.id.totalRestaurantsTextView).text =
                    "저장된 맛집 수: ${statistics.totalRestaurants} 개"
                findViewById<TextView>(R.id.favoriteCategoryTextView).text =
                    "선호하는 음식 카테고리: ${statistics.favoriteCategory}"
                findViewById<TextView>(R.id.favoriteMenuTextView).text =
                    "좋아하는 메뉴: ${statistics.favoriteMenu}"
                findViewById<TextView>(R.id.reviewsThisMonthTextView).text =
                    "이번달 작성된 맛집 수: ${statistics.reviewsThisMonth} 개"
            }
        }
        /**
         * 뒤로 가기 버튼 클릭 리스너 설정
         * MainActivity로 이동하고 현재 액티비티를 종료
         */
        binding.topAppBar.setNavigationOnClickListener {
            goToMainActivity()
        }
    }

    /**
     * 네비게이션바
     */
    private fun setupBottomNavigation() {
        bnv = findViewById(R.id.bnv_main)

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

    private fun goToMainActivity() {
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

    inner class StatisticsManager {

        fun calculateStatistics(callback: (Statistics) -> Unit) {
            firebaseManager.loadRestaurantData { restaurants ->
                val totalRestaurants = restaurants.size

                // 선호하는 음식 카테고리 계산
                val favoriteCategory = restaurants.groupBy { it.category }
                    .maxByOrNull { it.value.size }
                    ?.key ?: "Unknown"

                // 좋아하는 메뉴 계산
                val favoriteMenu = restaurants.groupBy { it.menu }
                    .maxByOrNull { it.value.size }
                    ?.key ?: "Unknown"

                // 이번 달 작성된 맛집 수 계산
                val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                val reviewsThisMonth = restaurants.count {
                    it.reviewDate.startsWith(currentMonth)
                }

                val statistics = Statistics(
                    totalRestaurants = totalRestaurants,
                    favoriteCategory = favoriteCategory,
                    favoriteMenu = favoriteMenu,
                    reviewsThisMonth = reviewsThisMonth
                )

                callback(statistics)
            }
        }
    }
}
