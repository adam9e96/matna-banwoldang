package com.example.restaurantreview.main.view

import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityBanner2Binding
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class Banner2Activity : AppCompatActivity() {

    private lateinit var binding: ActivityBanner2Binding
    private lateinit var bnv: BottomNavigationView
    private val firebaseManager: FirebaseManager = FirebaseManager(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBanner2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTextViews()

        setupTopAppBar()

        setupBottomNavigation()
    } // onCreate() END


    private fun setupTextViews() {
        binding.leaderGithub.text = HtmlCompat.fromHtml(
            "<a href=\"https://github.com/Fpkm9999\">깃허브 : https://github.com/Fpkm9999</a>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.leaderGithub.movementMethod = LinkMovementMethod.getInstance()

        binding.memberGithub.text = HtmlCompat.fromHtml(
            "<a href=\"https://github.com/shtmdgh0108\">깃허브 : https://github.com/shtmdgh0108</a>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.memberGithub.movementMethod = LinkMovementMethod.getInstance()

        binding.member2Github.text = HtmlCompat.fromHtml(
            "<a href=\"https://github.com/shtmdgh0108\">깃허브 : https://github.com/shtmdgh0108</a>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        binding.member2Github.movementMethod = LinkMovementMethod.getInstance()
    }


    /**
     * 네비게이션바
     */
    private fun setupBottomNavigation() {
        bnv = binding.bnvMain

        bnv.menu.getItem(0).isChecked = true

        bnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naviHome -> {
                    goToMainActivity()
                    true
                }

                R.id.naviSearch -> {
                    goToMapsActivity()
                    true
                }

                R.id.naviFavorite -> {
                    goToBookmarkActivity()
                    true
                }

                R.id.naviAdd -> {
                    goToAddRestaurantDataActivity()
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

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
