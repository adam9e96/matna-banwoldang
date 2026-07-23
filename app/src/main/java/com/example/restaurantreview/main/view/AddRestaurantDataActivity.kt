package com.example.restaurantreview.main.view

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.BounceInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityAddRestaurantDataBinding
import com.example.restaurantreview.main.dialog.AddCheckedDialog
import com.example.restaurantreview.main.dialog.AddCheckedDialogInterface
import com.example.restaurantreview.main.dialog.CancelDialog
import com.example.restaurantreview.main.dialog.CancelDialogInterface
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRestaurantDataActivity : AppCompatActivity(), CancelDialogInterface,
    AddCheckedDialogInterface {
    private val binding by lazy { ActivityAddRestaurantDataBinding.inflate(layoutInflater) }

    // 애니메이션 관련 필수 사항
    lateinit var scaleAnimation: ScaleAnimation

    private val restaurantManager = FirebaseManager(this@AddRestaurantDataActivity)

    // 데이터 클래스
    private var imageURl1: Uri = Uri.parse("")
    private var store: String = "" // 가게이름
    private var menu: String = "" // 추천메뉴
    private var review: String = "" // 리뷰
    private var rating: Float = 0.0f // 평점
    private var latitude: Double = 0.0 // 위도
    private var longitude: Double = 0.0 // 경도
    private lateinit var tags: List<String> // 태그
    private var businessDays: String = "" // 영업일
    private var businessHours: String = "" // 영업 시간 00:00-00:00
    private var breakTime: String = "" // 브레이크타임 00:00-00:00
    private var bookmark: Boolean = false // 좋아요
    private var category: String = "" // 카테고리
    private var imageURl: String = "" // 이미지 경로
    private var reviewDate: String = "" // 등록일
    private var nullCheckedList = mutableListOf<String>() // null 체크용 리스트
    private var busunessHour1: String = "" // 영업시간 첫번쨰 받아놓을거
    private var breakTime1: String = "" // 브레이크 타임 첫번쨰 받아놓을거
    // 프래그먼트 값 확인용
    private var firstFragment = "First"
    private var secondFragment = "Second"
    private var thirdFragment = "Third"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setFragment(firstFragment,AddRestaurantDataFragment())

        checkPermissions()

        // 애니메이션 효과주기
        scaleAnimation = ScaleAnimation(
            0.7f,
            1.0f,
            0.7f,
            1.0f,
            Animation.RELATIVE_TO_SELF,
            0.7f,
            Animation.RELATIVE_TO_SELF,
            0.7f
        ).apply {
            duration = 500
            interpolator = BounceInterpolator()
        }

        binding.run {
            // TabLayout 탭 선택시 화면 전환
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        0 -> setFragment(firstFragment ,AddRestaurantDataFragment())
                        1 -> nullChecked(AddRestaurantReview())
                        2 -> nullCheckedReview(AddRestaurantMaps())
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })

            // 뒤로가기 버튼 활성화
            topAppBar.setNavigationOnClickListener {
                val title = "정말 되돌아 갑니까?"
                dialog(title)
            }

            // 하트 버튼
            buttonFavorite.setOnCheckedChangeListener { compoundButton, isChecked ->
                compoundButton.startAnimation(scaleAnimation)
                receiveBoolean(isChecked)
            }

            compatButton.setOnClickListener {
                addUri()
                val intent = Intent(this@AddRestaurantDataActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            cancelButton.setOnClickListener {
                val title = "정말 취소하시겠습니까?"
                dialog(title)
            }
        }
    }


    //    private fun setFragment(tag: String ,frag: Fragment) {  // 프래그먼트 화면 전환 메서드
//        supportFragmentManager.commit {
//            replace(R.id.frame1, frag)
//            setReorderingAllowed(true)
//            addToBackStack(null)
//        }
//    }
    private fun setFragment(tag: String ,frag: Fragment) {  // 프래그먼트 화면 전환 메서드
        val manager : FragmentManager = supportFragmentManager
        val bt = manager.beginTransaction()
        if (manager.findFragmentByTag(tag) == null){
            bt.add(R.id.frame1, frag, tag)
        }
        val data = manager.findFragmentByTag(firstFragment)
        val review = manager.findFragmentByTag(secondFragment)
        val maps = manager.findFragmentByTag(thirdFragment)
        if (data != null) {
            bt.hide(data)
        }
        if (review != null) {
            bt.hide(review)
        }
        if (maps != null) {
            bt.hide(maps)
        }

        //tag로 입력받은 fragment만 show를 통해 보여주도록 합니다.
        if (tag == firstFragment) {
            if (data != null) {
                bt.show(data)
            }
        }
        else if (tag == secondFragment) {
            if (review != null) {
                bt.show(review)
            }
        }
        else if (tag == thirdFragment) {
            if (maps != null) {
                bt.show(maps)
            }
        }
        bt.commitAllowingStateLoss()

    }

    // 권한 확인
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

    // 뒤로가기 + 취소 버튼 클릭시 나올 다이얼로그 창
    private fun dialog(title: String) {
        val content = "작성 하신 내용은 전부 삭제 됩니다."
        val dialog = CancelDialog(this@AddRestaurantDataActivity, title, content, "확인", 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "Cancel")
    }

    override fun onClickYesButton(id: Int) {
        val intent = Intent(this@AddRestaurantDataActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // 가게정보 null 체크
    private fun nullChecked(frag: Fragment) {
        if (store == "" || businessDays == "" || businessHours == "" || busunessHour1 == "" || category == "" || imageURl1 == Uri.parse(
                ""
            ) || nullCheckedList.isEmpty()
        ) {
            val content = mutableListOf<String>()

            if (store == "") {
                content.add("가게이름")
            }
            if (businessDays == "") {
                content.add("영업일")
            }
            if (businessHours == "") {
                content.add("영업시간")
            }
            if (busunessHour1 == "") {
                content.add("영업시간2")
            }

            if (category == "") {
                content.add("카테고리")
            }
            if (imageURl1 == Uri.parse("")) {
                content.add("이미지")
            }
            if (nullCheckedList.isEmpty()) {
                content.add("태그")
            }
            nullCheckedDialog(content.toString().replace("[", "").replace("]", ""))
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0), true)

        } else {
            setFragment(secondFragment,frag)
        }
    }

    // 가게 리뷰 널 체크
    private fun nullCheckedReview(frag: Fragment) {
        val content = mutableListOf<String>()
        if (review == "" || rating == 0.0f) {
            if (review == "") {
                content.add("리뷰")
            }
            if (rating == 0.0f) {
                content.add("평점")
            }
            nullCheckedDialog(content.toString().replace("[", "").replace("]", ""))
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1), true)
        } else {
            setFragment(thirdFragment,frag)
            binding.compatButton.isEnabled = true
        }
    }

    // null 체크 공통 dialog
    private fun nullCheckedDialog(content: String) {
        val dialog = AddCheckedDialog(this@AddRestaurantDataActivity, content, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "nullChecked")
    }

    override fun onClickCheckedYesButton(id: Int, content: String?) {
        Toast.makeText(applicationContext, "$content 를 입력해 주세요.", Toast.LENGTH_SHORT).show()
    }

    // 가게이름 editText 값 가져오기
    fun receiveName(name: String) {
        Log.i("summmmed", name) // 값이 넘어 오는것까지 확인됨.
        store = name
    }

    // 가게 추천 메뉴
    fun receiveMenu(menu1: String) {
        Log.i("summmmed", menu1) // 값이 넘어 오는것까지 확인됨.
        menu = menu1
    }

    // 가게 리뷰
    fun receiveReview(review1: String) {
        Log.i("summmmed", review1) // 값이 넘어 오는것까지 확인됨.
        review = review1
    }

    // 레이팅 바 숫자 들고오기
    fun receiveRating(rating2: Float) {
        Log.i("summmmrating", rating2.toString())
        rating = rating2
    }

    // 카테고리 받아오는거
    fun receiveData(edit: String) {
        category = edit
        // Log.i("summmm", ed1) // 값이 넘어 오는것까지 확인됨.
    }

    // 태그 리스트 입력받는부분
    fun receiveList(list: MutableList<String>) {
        nullCheckedList = list
        tags = nullCheckedList
        // Log.i("summmm1", ed2.toString()) // 값이 잘 넘어 오는거 확인됨.
    }

    // 요일 리스트로 받는부분
    fun receiveDayList(list: MutableList<String>) {
        businessDays = list.toString().replace("]", "")
        Log.i("summmm2", list.toString()) // 값이 잘 넘어 오는거 확인됨.
    }

    // 영업시간 첫번째 받는부분
    fun receiveBusinessHourStart(time: String) {
        busunessHour1 = time
        Log.i("summmm4", time) // 값이 잘 넘어 오는거 확인됨.
    }

    // 영업시간 두번째
    fun receiveBusinessHourEnd(time: String) {
        businessHours = "$busunessHour1-$time"
        Log.i("summmm5", businessHours) // 값이 잘 넘어 오는거 확인됨.
    }

    fun receiveBreakTimeStart(time: String): String { // 브레이크 타임 첫번째
        breakTime1 = if (time == "00 : 00") {
            ""
        } else {
            time
        }
        Log.i("summmm6", breakTime1) // 값이 잘 넘어 오는거 확인됨.
        return breakTime1
    }

    fun receiveBreakTimeEnd(time: String) {
        breakTime = if (time == "00 : 00") {
            ""
        } else {
            "$breakTime1-$time"
        }
        Log.i("summmm7", breakTime) // 값이 잘 넘어 오는거 확인됨.
    }

    // 위도 경도 값 들고와보자
    fun receiveMarker(latitude1: Double, longitude1: Double) {
        latitude = latitude1
        longitude = longitude1
        Log.i("latlong!!", "$latitude, $longitude")
    }

    // Boolean 값 넘겨 받기
    fun receiveBoolean(tf: Boolean) {
        bookmark = tf
        Log.i("tureflase", "$bookmark")
    }

    // url값 받아 오는거
    fun receiveUrl(data: String) {
        imageURl1 = Uri.parse(data)
    }

    private fun addUri() {
        restaurantManager.uploadImageToFirebase(
            this@AddRestaurantDataActivity,
            imageURl1,
            store
        ) { isSuccess, fbUrl ->
            if (isSuccess) {
                Toast.makeText(this, "Image success : $fbUrl", Toast.LENGTH_SHORT).show()
                if (fbUrl != null) {
                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    imageURl = fbUrl
                    val newRestaurant = Restaurant(
                        store = store,
                        breakTime = breakTime,
                        menu = menu,
                        tags = tags,
                        rating = rating,
                        review = review,
                        reviewDate = today,
                        bookmark = bookmark,
                        category = category,
                        latitude = latitude,
                        longitude = longitude,
                        businessHours = businessHours,
                        businessDays = businessDays.replace("[", ""),
                        imageURl = imageURl
                    )
                    // 레스토랑 데이터 삽입
                    restaurantManager.addRestaurant(newRestaurant)
                }
            } else {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
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
}