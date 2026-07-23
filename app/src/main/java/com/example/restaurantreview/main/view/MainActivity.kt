package com.example.restaurantreview.main.view

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.elvishew.xlog.XLog
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityMainBinding
import com.example.restaurantreview.databinding.ItemCarouselBinding
import com.example.restaurantreview.databinding.ItemRestaurantBinding
import com.example.restaurantreview.main.dialog.RePermissionDialog
import com.example.restaurantreview.main.dialog.RePermissionDialogInterface
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.example.restaurantreview.main.settings.CustomToast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.android.material.carousel.FullScreenCarouselStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity(), RePermissionDialogInterface {
    private lateinit var binding: ActivityMainBinding
    private lateinit var restaurantList: MutableList<Restaurant>
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var imageRes: Array<Int>
    private lateinit var bnv: BottomNavigationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var lastSelectedChip: Chip? = null
    private var isTagExpanded = false
    private var firebaseManager: FirebaseManager = FirebaseManager(this)
    private var selectedCategory: String? = null
    private var selectedTags: List<String> = emptyList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()

        // 하단 내비게이션바
        setupBottomNavigation()

        // 서치바
        setupSearchBar()

        // 배너
        setupBanner()

        // 카테고리
        setupCategory()

        // 태그
        setupTag()

        // 조회 아이템
        setupRecyclerView()

        setupSwipeRefresh()

        // Firebase에서 데이터 로드
        firebaseManager.loadRestaurantData { restaurants ->
            restaurantList.clear()
            restaurantList.addAll(restaurants)
            restaurantAdapter.notifyDataSetChanged()
        }
    } // onCreate() END

    // 위치 권한을 확인하고 요청하는 함수
    private fun checkPermissions() {
        if (!(ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermission()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1000
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, "권한설정 성공", Toast.LENGTH_SHORT).show()
        } else {
            requestPermissionDialog()
        }
    }

    override fun permissionYesButton(id: Int) {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission()
        }
    }

    override fun permissionNoButton(id: Int) {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    // ==============================================

    /**
     * 네비게이션바
     */
    private fun setupBottomNavigation() {
        bnv = binding.bnvMain

        bnv.menu.getItem(0).isChecked = true

        bnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.naviHome -> {// 메인페이지
                    startActivity(this.intent)
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

    // ====================================================

    /**
     * 서치바를 설정하는 메서드
     */
    private fun setupSearchBar() {
        val searchBar: SearchBar = binding.mainSearchBar
        val searchView: SearchView = binding.mainSearchView


        searchBar.setOnClickListener {
            searchView.show() // SearchView 표시
        }

        searchView.findViewById<View>(R.id.main_search_view)
            .setOnClickListener { searchView.hide() }


        searchView.editText.setOnEditorActionListener { _, actionId, _ ->
            Log.d("adb", "actionId: $actionId")


            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val inputText = searchView.text.toString()

                XLog.d("setupSearchBar() - 검색 : $inputText")

                // 검색을 수행
                firebaseManager.searchRestaurants(inputText) { restaurants ->
                    // 전체보기 텍스트 검색결과로 수정
                    binding.mainRestaurantTitle.text =
                        String.format(getString(R.string.search_result), inputText)
                    setRestaurants(restaurants)

                    // 검색 결과로 스크롤 이동
                    binding.scrollView.post {
                        binding.scrollView.smoothScrollTo(0, binding.mainRestaurantTitle.bottom)
                    }
                }

                Toast.makeText(this, "입력된 값: $inputText", Toast.LENGTH_SHORT).show()
                // searchView에 입력된 값을 초기화
                searchView.clearText()
                // searchView 숨김
                searchView.hide() // 검색 후 SearchView 숨기기
                true
            } else {
                false
            }
        }
    }


    /**
     * 검색 결과를 UI에 반영하는 메서드
     */
    private fun setRestaurants(newRestaurants: List<Restaurant>) {
        val oldSize = restaurantList.size
        restaurantList.clear()
        restaurantList.addAll(newRestaurants)
        // 이전 항목에 대해 항목 범위 제거 알림과 새로운 항목에 대해 항목 범위 삽입 알림을 사용
        restaurantAdapter.notifyItemRangeRemoved(0, oldSize)
        restaurantAdapter.notifyItemRangeInserted(0, newRestaurants.size)
    }

    // ====================================

    /**
     * 배너를 설정하는 메서드
     */
    private fun setupBanner() {

        // 이미지 리스트 초기화
        imageRes = arrayOf(
            R.drawable.caroll_01,
            R.drawable.caroll_02,
            R.drawable.caroll_03
        )
        val bannerView = binding.carouselRecyclerView
        // RecyclerView 설정
        bannerView.apply {
            adapter = CarouselAdapter(imageRes)
            layoutManager = CarouselLayoutManager(FullScreenCarouselStrategy())
        }
    }

    // ========================================================


    /**
     * 태그 칩들을 설정하는 메서드
     */
    private fun setupTag() {
        val chipGroup = binding.mainTagGroup
        val showMoreButton = binding.mainTagShowButton

        // 기본적으로 5개의 칩만 보이게 설정
        val initialVisibleChips = 5
        for (i in initialVisibleChips until chipGroup.childCount) {
            chipGroup.getChildAt(i).visibility = View.GONE
        }

        // "더 보기" 버튼 클릭 리스너 설정
        showMoreButton.setOnClickListener {
            if (isTagExpanded) {
                // 칩을 다시 숨기기
                for (i in initialVisibleChips until chipGroup.childCount) { // 5 to 24
                    chipGroup.getChildAt(i).visibility = View.GONE
                }
                showMoreButton.text = "더 보기"
            } else {
                // 모든 칩을 보이기
                for (i in initialVisibleChips until chipGroup.childCount) {
                    chipGroup.getChildAt(i).visibility = View.VISIBLE
                }
                showMoreButton.text = "간단히 보기"
            }
            isTagExpanded = !isTagExpanded
        }
        // 칩 그룹에 속한 칩이 변경될 때 호출될 콜백을 등록
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            handleTagChipClick(group, checkedIds)
        }
    }

    /**
     * 태그 칩 클릭 이벤트를 처리하는 메서드
     */
    private fun handleTagChipClick(chipGroup: ChipGroup, checkedIds: List<Int>) {
        selectedTags = checkedIds.mapNotNull { id ->
            chipGroup.findViewById<Chip>(id)?.text?.toString()?.replace("#", "")
        }

        // 전체보기 텍스트 검색결과로 수정
        binding.mainRestaurantTitle.text = when {
            selectedTags.isNotEmpty() && selectedCategory.isNullOrEmpty() -> {
                "검색결과 \n 태그 : $selectedTags"
            }

            selectedTags.isEmpty() && selectedCategory.isNullOrEmpty() -> {
                "전체 보기"
            }

            selectedTags.isNotEmpty() && !selectedCategory.isNullOrEmpty() -> {
                "검색 결과 \n 카테고리 : $selectedCategory \n 태그 : $selectedTags"
            }

            selectedTags.isEmpty() && !selectedCategory.isNullOrEmpty() -> {
                "검색 결과 \n 카테고리 : $selectedCategory"
            }

            else -> {
                "전체 보기"
            }
        }


//            String.format(getString(R.string.search_result_category_tags),selectedCategory, selectedTags)

        XLog.d("handleTagChipClick() - 선택된 태그 : $selectedTags")
        XLog.d("handleTagChipClick() - 현재 선택된 카테고리 : [$selectedCategory], Tags: $selectedTags")

        loadRestaurants(selectedCategory, selectedTags)
    }

    /**
     * 카테고리 칩들을 설정하는 메서드
     */
    private fun setupCategory() {
        val chipGroup = binding.mainCategoryGroup

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            handleCategoryChipClick(group, checkedIds)
        }
    }

    /**
     * 카테고리 칩 클릭 이벤트를 처리하는 메서드
     */
    private fun handleCategoryChipClick(chipGroup: ChipGroup, checkedIds: List<Int>) {
        val chip: Chip? = checkedIds.firstOrNull()?.let { chipGroup.findViewById(it) }

        selectedCategory = chip?.text?.toString()

        // 전체보기 텍스트 검색결과로 수정
        binding.mainRestaurantTitle.text = when {
            selectedTags.isNotEmpty() && selectedCategory.isNullOrEmpty() -> {
                "검색결과 \n 태그 : $selectedTags"
            }

            selectedTags.isEmpty() && selectedCategory.isNullOrEmpty() -> {
                "전체 보기"
            }

            selectedTags.isNotEmpty() && !selectedCategory.isNullOrEmpty() -> {
                "검색 결과 \n 카테고리 : $selectedCategory \n 태그 : $selectedTags"
            }

            selectedTags.isEmpty() && !selectedCategory.isNullOrEmpty() -> {
                "검색 결과 \n 카테고리 : $selectedCategory"
            }

            else -> {
                "전체 보기"
            }
        }

        lastSelectedChip?.isChecked = false

        if (chip == lastSelectedChip) {
            chip?.isChecked = false
            lastSelectedChip = null
            selectedCategory = null
        } else {
            chip?.isChecked = true
            lastSelectedChip = chip
        }

        XLog.d("handleCategoryChipGroupClick() - [$selectedCategory], 태그: $selectedTags ")
        loadRestaurants(selectedCategory, selectedTags)
    }


    private fun setupRecyclerView() {
        // restaurant(맛집) 조회페이지 관련
        restaurantList = mutableListOf() // 가변 리스트 초기화

        // 아래 어댑터 메인 엑티비티에 내부클래스로 합치기
        restaurantAdapter = RestaurantAdapter(restaurantList) { restaurant ->
            val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
                Log.d("테스트테스트", restaurant.toString())
                putExtra("restaurant", restaurant)
            }
            startActivity(intent)
        }
        val restaurantListView = binding.mainRestaurantList

        restaurantListView.apply {
            adapter = restaurantAdapter
            layoutManager = GridLayoutManager(this@MainActivity, 2)
        }
    }


    /**
     * 모든 레스토랑을 로드하는 메서드
     */
    private fun loadAllRestaurants() {
        firebaseManager.loadRestaurantData { restaurants ->
            restaurantList.clear()
            restaurantList.addAll(restaurants)
            restaurantAdapter.notifyDataSetChanged()
        }
    }

    /**
     * 카테고리와 태그를 기준으로 레스토랑을 로드하는 메서드
     */
    private fun loadRestaurants(category: String?, tags: List<String>?) {
        if (category.isNullOrEmpty() && tags.isNullOrEmpty()) {
            // 카테고리와 태그가 선택되지 않은 경우 모든 데이터를 로드
            loadAllRestaurants()
            XLog.d("loadRestaurants() - 모든 맛집 데이터를 로딩합니다.")
            return
        }
        // 카테고리 또는 태그가 있는 경우 해당 데이터를 로드
        XLog.d("loadRestaurants() - 카테고리나 태그에 해당되는 맛집 데이터를 로드합니다.")
        firebaseManager.getRestaurantsByCategoryAndTags(
            category = category,
            tags = tags
        ) { newRestaurants ->
            val oldSize = restaurantList.size
            if (newRestaurants.isNotEmpty()) {
                restaurantList.clear()
                restaurantList.addAll(newRestaurants)
                // 이전 항목에 대해 항목 범위 제거 알림과 새로운 항목에 대해 항목 범위 삽입 알림을 사용
                restaurantAdapter.notifyItemRangeRemoved(0, oldSize)
                restaurantAdapter.notifyItemRangeInserted(0, newRestaurants.size)
                XLog.d("loadRestaurants() - ${newRestaurants.size} 개의 새로운 맛집 로드완료!")

            } else {
                restaurantList.clear()
                restaurantAdapter.notifyDataSetChanged()
                Toast.makeText(this, "일치하는 맛집이 없습니다.", Toast.LENGTH_SHORT).show()
                XLog.d("loadRestaurants() - 일치하는 맛집이 없습니다.")
            }
        }
    }


    // 뷰홀더 관련 어댑터 내부 클래스
    inner class CarouselAdapter(private val imageRes: Array<Int>) :
        RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

        inner class CarouselViewHolder(private val binding: ItemCarouselBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        handleCarouselItemClick(position)
                    }
                }
            }

            fun bind(imageResId: Int) {
                binding.carouselImageView.setImageResource(imageResId)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
            val binding =
                ItemCarouselBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CarouselViewHolder(binding)
        }

        override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
            holder.bind(imageRes[position])
//            Log.d("Carousel_click_test", "${imageRes[position]}")
        }

        override fun getItemCount(): Int = imageRes.size
    }

    // 캐러셀 뷰 클릭시
    private fun handleCarouselItemClick(position: Int) {
        val intent = when (position) {
            0 -> {
                XLog.d("handleCarouselItemClick() - 배너1 페이지 이동")
                Intent(this, Banner1Activity::class.java)
            }

            1 -> {
                XLog.d("handleCarouselItemClick() - 배너2 페이지 이동")
                Intent(this, Banner2Activity::class.java)
            }

            2 -> {
                XLog.d("handleCarouselItemClick() - 배너3 페이지 이동")
                Intent(this, Banner3Activity::class.java)
            }

            else -> return
        }
        startActivity(intent)
    }
    // 맛집 조회 관련 어댑터 내부 클래스


    /**
     * RecyclerView 어댑터 클래스
     * 레스토랑 데이터를 그리드 형태로 화면에 표시합니다.
     *
     * @param restaurantList 표시할 레스토랑 데이터 리스트
     */
    inner class RestaurantAdapter(
        private var restaurantList: MutableList<Restaurant>,
        private val onItemClickListener: (Restaurant) -> Unit
    ) : RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>() {

        /**
         * RecyclerView.ViewHolder를 상속받아 ViewHolder를 정의합니다.
         * ViewHolder는 각 아이템의 뷰를 보유하며 뷰의 데이터를 설정합니다.
         */
        inner class RestaurantViewHolder(binding: ItemRestaurantBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val imageView = binding.rsImage
            val storeName = binding.rsName
            val reviewDate = binding.rsReviewDate
            val rating = binding.rsRating
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RestaurantViewHolder {
            val binding =
                ItemRestaurantBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return RestaurantViewHolder(binding)
        }

        override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
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
            holder.itemView.setOnClickListener {
                onItemClickListener(restaurant)

            }
        }

        override fun getItemCount() = restaurantList.size
    }

    private fun refreshContent() {
        // 데이터를 새로고침하는 작업을 수행합니다
        // 예: 데이터를 다시 가져오거나 화면을 업데이트합니다
        loadAllRestaurants()

        CustomToast.createToast(this@MainActivity, "Refreshed!")


        // 새로고침 완료 후 SwipeRefreshLayout을 비활성화합니다
        swipeRefreshLayout.isRefreshing = false
    }

    private fun requestPermissionDialog() {
        val dialog = RePermissionDialog(this@MainActivity, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "permission")
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }
    }

}