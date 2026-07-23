package com.example.restaurantreview.main.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityBanner3Binding
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class Banner3Activity : AppCompatActivity() {
    private lateinit var binding: ActivityBanner3Binding
    private lateinit var bnv: BottomNavigationView
    private var firebaseManager: FirebaseManager = FirebaseManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBanner3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        /**
         * 뒤로 가기 버튼 클릭 리스너 설정
         * MainActivity로 이동하고 현재 액티비티를 종료
         */
        binding.topAppBar.setNavigationOnClickListener {
            navigateToMainActivity()
        }

        binding.imageView.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + applicationContext.packageName)
            }
            startActivity(intent)
        }
        binding.iconInfo.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:" + applicationContext.packageName)
            }
            startActivity(intent)
        }
        setupBottomNavigation()



    } // onCreate() END

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * 네비게이션바
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
}