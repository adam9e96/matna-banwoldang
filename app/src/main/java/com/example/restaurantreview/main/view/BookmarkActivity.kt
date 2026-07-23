package com.example.restaurantreview.main.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityBookmarkBinding
import com.example.restaurantreview.databinding.ItemBookmarkBinding
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.example.restaurantreview.main.settings.CustomToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.materialswitch.MaterialSwitch
import com.squareup.picasso.Picasso


/**
 * 북마크된 레스토랑을 표시하는 액티비티
 *
 * 주요 기능:
 * - 인텐트로 전달된 레스토랑 리스트를 받아와 RecyclerView에 표시
 * - 북마크 상태 변경 시 Firebase에 업데이트
 * - 하단 내비게이션을 통한 페이지 이동
 */
class BookmarkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarkBinding
    private lateinit var restaurantList: MutableList<Restaurant>
    private var firebaseManager: FirebaseManager = FirebaseManager(this)
    private lateinit var adapter: BookmarkAdapter
    private lateinit var bnv: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarkBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // 인텐트에서 데이터를 가져옴
        restaurantList =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra("restaurantList", Restaurant::class.java)
                    ?: mutableListOf()
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra("restaurantList") ?: mutableListOf()
            }

        // RecyclerView 설정
        setupRecyclerView()

        // 하단 내비게이션바 관련
        setupBottomNavigation()

    } // onCreate() END

    /**
     * 네비게이션바
     */
    private fun setupBottomNavigation() {
        bnv = binding.bnvMain
        bnv.menu.getItem(0).isChecked = false
        bnv.menu.getItem(2).isChecked = true

        bnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naviHome -> {// 메인페이지
                    goToMainActivity()
                    true
                }

                R.id.naviSearch -> {// 지도 조회페이지
                    goToMapsActivity()
                    true
                }

                R.id.naviFavorite -> {// 좋아요 페이지
                    startActivity(this.intent)
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

    private fun goToMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // 등록페이지로 보내는 인텐트
    private fun goToAddRestaurantDataActivity() {
        val intent = Intent(this, AddRestaurantDataActivity::class.java)
        startActivity(intent)
    }

    /**
     * RecyclerView 설정
     */
    private fun setupRecyclerView() {
        adapter = BookmarkAdapter(restaurantList) { restaurant ->
            // 아이템 클릭 시 RestaurantDetailActivity로 이동
            val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra("restaurant", restaurant)
            }
            startActivity(intent)
        }
        binding.restaurantBookmarked.layoutManager = LinearLayoutManager(this)
        binding.restaurantBookmarked.adapter = adapter

        CustomToast.createToast(this@BookmarkActivity, "북마크 로드 완료! ${restaurantList.size} 개!")

    }

    /**
     * 북마크된 레스토랑을 표시하는 RecyclerView 어댑터
     *
     * @param restaurantList 표시할 레스토랑 리스트
     * @param onItemClickListener 아이템 클릭 리스너
     */
    inner class BookmarkAdapter(
        private val restaurantList: MutableList<Restaurant>,
        private val onItemClickListener: (Restaurant) -> Unit
    ) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

        /**
         * 뷰 홀더 클래스
         * @param binding 아이템 뷰 바인딩
         */
        inner class BookmarkViewHolder(val binding: ItemBookmarkBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val layout = binding.root
            val imageView = binding.restaurantImage
            val storeName = binding.restaurantName
            val reviewDate = binding.restaurantReviewDate
            val rating = binding.restaurantRating
            val menu = binding.menu
            val bookmarkSwitch: MaterialSwitch = binding.bookmarkSwitch
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
            val binding =
                ItemBookmarkBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BookmarkViewHolder(binding)
        }

        override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
            val restaurant = restaurantList[position]

            Picasso.get()
                .load(restaurant.imageURl)
                .resize(300, 300)
                .centerInside()
                .into(holder.imageView)

            holder.storeName.text = restaurant.store
            holder.reviewDate.text = restaurant.reviewDate
            holder.rating.text = holder.itemView.context.getString(
                R.string.rs_rating_string,
                restaurant.rating.toString()
            )
            holder.menu.text = restaurant.menu
            holder.bookmarkSwitch.isChecked = restaurant.bookmark

            holder.layout.animation =
                AnimationUtils.loadAnimation(holder.layout.context, R.anim.list_item_animation)

            holder.bookmarkSwitch.setOnCheckedChangeListener { _, isChecked ->
                restaurant.bookmark = isChecked
                firebaseManager.updateRestaurant(restaurant.id, restaurant)

                if (!isChecked) {
                    removeItem(position)
                }
            }
            holder.itemView.setOnClickListener {
                onItemClickListener(restaurant)
            }
        }

        override fun getItemCount() = restaurantList.size

        private fun removeItem(position: Int) {
            restaurantList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, restaurantList.size)
        }
    }

}
