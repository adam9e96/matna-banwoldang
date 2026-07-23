package com.example.restaurantreview.main.view

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.elvishew.xlog.XLog
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ActivityRestaurantDetailModBinding
import com.example.restaurantreview.main.dialog.*
//import com.example.restaurantreview.main.dialog.DayDialog
//import com.example.restaurantreview.main.dialog.DayDialogInterface
//import com.example.restaurantreview.main.dialog.ModBottomNaviDialog
//import com.example.restaurantreview.main.dialog.ModBottomNaviDialogInterface
//import com.example.restaurantreview.main.dialog.ModCategoryDialog
//import com.example.restaurantreview.main.dialog.ModCategoryDialogInterface
//import com.example.restaurantreview.main.dialog.ModImageDialog
//import com.example.restaurantreview.main.dialog.ModImageDialogInterface
//import com.example.restaurantreview.main.dialog.ModMapsDialog
//import com.example.restaurantreview.main.dialog.ModMapsDialogInterface
//import com.example.restaurantreview.main.dialog.ModRePermissionDialog
//import com.example.restaurantreview.main.dialog.ModRePermissionDialogInterface
//import com.example.restaurantreview.main.dialog.ModTagDialog
//import com.example.restaurantreview.main.dialog.ModTagDialogInterface
import com.example.restaurantreview.main.firebase.FirebaseManager
import com.example.restaurantreview.main.model.Restaurant
import com.example.restaurantreview.main.settings.CustomToast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date

class RestaurantDetailActivityMod : AppCompatActivity(), OnMapReadyCallback, DayDialogInterface,
    ModCategoryDialogInterface, ModTagDialogInterface, ModImageDialogInterface,
    ModRePermissionDialogInterface, ModMapsDialogInterface, ModBottomNaviDialogInterface {
    private lateinit var binding: ActivityRestaurantDetailModBinding
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var daylist: String = ""
    private var category: String = ""
    private var tag = mutableListOf<String>()
    private val PERMISSION_GALLERY = 101 // 앨범 권한 처리
    private var data2: String = ""
    private val CAMERA_CODE = 102
    private var ModimageURl: Uri = Uri.parse("") // 이미지 경로
    private val firebaseManager = FirebaseManager(this@RestaurantDetailActivityMod)
    private lateinit var bnv: BottomNavigationView
    private lateinit var mapView: MapView
    private lateinit var mMap: GoogleMap
    private var newUpdatedImageURLToDetailActivity: String = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantDetailModBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val progressBar = binding.progressBar

        val restaurant: Restaurant? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("restaurant", Restaurant::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("restaurant")
        }

        val restaurantId: String? = intent.getStringExtra("restaurantId")
        Log.d("restaurantId 테스트", "$restaurantId")

        // 레스토랑 데이터로 UI 업데이트
        restaurant?.let {
            binding.restaurantName.setText(it.store)
            binding.restaurantMenu.setText(it.menu)
            binding.restaurantReview.setText(it.review)
            binding.restaurantRating.rating = (it.rating)
            lat = restaurant.latitude
            long = restaurant.longitude
            binding.restaurantTags.text = it.tags.joinToString(", ")
            binding.restaurantBusinessDays.text = it.businessDays
            binding.restaurantBusinessHours.text = it.businessHours.substring(
                0,
                it.businessHours.indexOf("-")
            )
            binding.restaurantBusinessHours2.text = it.businessHours.substring(
                it.businessHours.indexOf(
                    "-"
                ) + 1
            )
            binding.restaurantBreakTime.setText(
                if (it.breakTime == "") {
                    ""
                } else {
                    it.breakTime?.substring(0, it.breakTime.indexOf("-"))
                        ?: ""
                }
            )
            binding.restaurantBreakTime2.setText(
                if (it.breakTime == "") {
                    ""
                } else {
                    it.breakTime?.substring(it.breakTime.indexOf("-") + 1)
                        ?: ""
                }
            )
            binding.restaurantCategory.setText(it.category)
            binding.restaurantReviewDate.setText(it.reviewDate)
            binding.restaurantBookmark.isChecked = it.bookmark

            // 이미지 로드
            Picasso.get().load(it.imageURl).into(binding.detailRsImage)
        }

        // 브레이트 타임의 값이 null 일경우 버튼의 색을 하얀색으로 줘서 눈에 안보이게함
        if (binding.restaurantBreakTime.text == "") {
            binding.restaurantBreakTime.setBackgroundColor(0)
        }
        if (binding.restaurantBreakTime2.text == "") {
            binding.restaurantBreakTime2.setBackgroundColor(0)
        }
        mapView = binding.restaurantMap1
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        /**
         * 뒤로 가기 버튼 클릭 리스너 설정
         * RestaurantDetailActivity 액티비티로 전환되고 종료
         */
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        binding.restaurantBusinessDays.setOnClickListener {
            dialog()
        }
        binding.restaurantCategory.setOnClickListener {
            CategoryDialog()
        }
        binding.restaurantTags.setOnClickListener {
            TagDialog()
        }
        // 영업시간 설정
        binding.restaurantBusinessHours.setOnClickListener {
            getTime(binding.restaurantBusinessHours, binding.restaurantBusinessHours.context)
        }
        binding.restaurantBusinessHours2.setOnClickListener {
            getTime(binding.restaurantBusinessHours2, binding.restaurantBusinessHours2.context)
        }

        //브레이크 타임 설정
        binding.restaurantBreakTime.setOnClickListener {
            binding.restaurantBreakTime.setBackgroundColor(Color.parseColor("#6E3EB3"))
            getTime(binding.restaurantBreakTime, binding.restaurantBreakTime.context)
        }
        binding.restaurantBreakTime2.setOnClickListener {
            binding.restaurantBreakTime2.setBackgroundColor(Color.parseColor("#6E3EB3"))
            getTime(binding.restaurantBreakTime2, binding.restaurantBreakTime2.context)
        }
        binding.detailRsImage.setOnClickListener {
            requestPermission()
        }


        /**
         * 완료 버튼 클릭 리스너 설정
         */
        val completeBtn = binding.detailCompleteBtn

        completeBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE // ProgressBar 시작

            restaurant?.let { restaurant ->
                restaurantId?.let { id ->
                    updateRestaurant(id, restaurant) { updatedRestaurant ->
                        progressBar.visibility = View.GONE // ProgressBar 종료
                        val intent = Intent().apply {
                            putExtra("restaurantId", id)
                            putExtra("restaurant", updatedRestaurant)
                            putExtra("latitude", lat)
                            putExtra("longitude", long)
                            putExtra("imageUrl", newUpdatedImageURLToDetailActivity)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
        setupBottomNavigation()

    } // onCreate() END

    private fun updateRestaurant(
        id: String,
        restaurant: Restaurant,
        callback: (Restaurant) -> Unit
    ) {
        if (ModimageURl != Uri.parse("")) {

            firebaseManager.uploadImageToFirebase(
                this@RestaurantDetailActivityMod,
                ModimageURl,
                binding.restaurantName.text.toString()

            ) { isSuccess, newUpdatedImageUrl ->
                XLog.d("이미지 보자 - $newUpdatedImageUrl")
                if (isSuccess) {
                    Toast.makeText(this, "업로드 성공", Toast.LENGTH_SHORT).show()

//                    newUpdatedImageURLToDetailActivity = newUpdatedImageUrl.toString()

                    if (newUpdatedImageUrl != null) {
                        newUpdatedImageURLToDetailActivity = newUpdatedImageUrl.toString()

                        val updatedRestaurant = restaurant.copy(
                            store = binding.restaurantName.text.toString(),
                            menu = binding.restaurantMenu.text.toString(),
                            review = binding.restaurantReview.text.toString(),
                            rating = binding.restaurantRating.rating,
                            latitude = lat,
                            longitude = long,
                            tags = binding.restaurantTags.text.toString().split(",")
                                .map { it.trim() },
                            businessDays = binding.restaurantBusinessDays.text.toString(),
                            businessHours = "${binding.restaurantBusinessHours.text}-${binding.restaurantBusinessHours2.text}",
                            breakTime = "${binding.restaurantBreakTime.text}-${binding.restaurantBreakTime2.text}",
                            category = binding.restaurantCategory.text.toString(),
                            reviewDate = binding.restaurantReviewDate.text.toString(),
                            bookmark = binding.restaurantBookmark.isChecked,
                            imageURl = newUpdatedImageUrl
                        )
                        XLog.d("4")
                        XLog.d("update 객체테스트 - updatedRestaurant.toString()") // OK

                        firebaseManager.updateRestaurant(id, updatedRestaurant)
                        XLog.d("5")
                        callback(updatedRestaurant)
                    } else {

                        CustomToast.createToast(this@RestaurantDetailActivityMod, "실패 했다 바카야로")
                        XLog.d("실패함")

                    }
                }
            }
        } else {
            newUpdatedImageURLToDetailActivity = restaurant.imageURl
            val updatedRestaurant = restaurant.copy(
                store = binding.restaurantName.text.toString(),
                menu = binding.restaurantMenu.text.toString(),
                review = binding.restaurantReview.text.toString(),
                rating = binding.restaurantRating.rating,
                latitude = lat,
                longitude = long,
                tags = binding.restaurantTags.text.toString().split(",")
                    .map { it.trim() },
                businessDays = binding.restaurantBusinessDays.text.toString(),
                businessHours = "${binding.restaurantBusinessHours.text.toString()}-${binding.restaurantBusinessHours2.text.toString()}",
                breakTime = "${binding.restaurantBreakTime.text.toString()}-${binding.restaurantBreakTime2.text.toString()}",
                category = binding.restaurantCategory.text.toString(),
                reviewDate = binding.restaurantReviewDate.text.toString(),
                bookmark = binding.restaurantBookmark.isChecked
            )
            XLog.d("update 객체테스트 - ${updatedRestaurant.toString()}") // OK
            firebaseManager.updateRestaurant(id, updatedRestaurant)
            callback(updatedRestaurant)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map
        // Add a marker in Sydney and move the camera
        val daegu = LatLng(lat, long)
        mMap.setMaxZoomPreference(17f)
        mMap.setMinZoomPreference(16f)
        mMap.addMarker(
            MarkerOptions().position(daegu).title(binding.restaurantName.text.toString())
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(daegu, 16f))
        setMarker()
        mMap.setOnCameraChangeListener { setMarker() }
        mMap.setOnMapClickListener {
            ModMapsDialogCome(lat, long)

        }

    }

    // 마커 관련
    private fun setMarker() {

        // 마커 설정하는 함수
        mMap?.let {
            it.clear() // 지도에 있는 마커를 먼저 삭제
            val markerOptions = MarkerOptions()
            markerOptions.position(it.cameraPosition.target) // 마커의 위치 설정
            val marker = it.addMarker(markerOptions) // 지도에 마커를추가하고, 마커 객체를 리턴
            it.setOnCameraMoveListener {
                marker?.let { marker ->
                    marker.setPosition(it.cameraPosition.target)
                }
            }

            recevieMarker(
                markerOptions.position.latitude.toDouble(),
                markerOptions.position.longitude.toDouble()
            )


        }
    }

    fun recevieMarker(latitude1: Double, longitude1: Double) {
        lat = latitude1
        long = longitude1
        Log.i("latlong!!", "${lat}, ${long}")
    }

    private fun dialog() {
        val dialog = DayDialog(this@RestaurantDetailActivityMod, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "Day")

    }

    // 영업일 다이어로그에서 확인을 눌럿을경우
    override fun onClickYesButton(id: Int) {
        if (daylist != "") {
            binding.restaurantBusinessDays.text = daylist
        }
    }

    // 카테고리 다이어로그
    private fun CategoryDialog() {
        val dialog = ModCategoryDialog(this@RestaurantDetailActivityMod, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "Category")
    }

    // 날짜 다이오로그에서 날자값 받아오는 메소드
    fun receiveDayList(list: String) {
        daylist = list.replace("]", "")
    }

    // 카테고리 다이어로그에서 값 받아오는 메서드
    fun receiveCategory(category: String) {
        this.category = category
    }

    // 카테고리 다이어로그에서 확인을 눌럿을 경우
    override fun onClickCategoryYesButton(id: Int) {
        if (category != "") {
            binding.restaurantCategory.text = category
        }
    }

    // 태그 다이로그에서 값 받아오는 메서드
    fun receiveTag(tag: MutableList<String>) {
        this.tag = tag
    }

    //태그 다이어로그 실행
    private fun TagDialog() {
        val dialog = ModTagDialog(this@RestaurantDetailActivityMod, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "Tag")
    }

    // 태그 다이어로그에서 확인을 눌럿을 경우
    override fun onClickTagYesButton(id: Int) {
        XLog.d("태그 테스트 - ${tag.toString()}")
        if (tag.toString() != "[]") {
            binding.restaurantTags.text = tag.joinToString(", ")
        }
    }

    // 시간 가져다 붙이기
    private fun getTime(button: Button, context: Context) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

//            button.text = SimpleDateFormat("HH:mm").format(cal.time)
            button.text = when {
                hour < 10 -> {
                    if (minute < 10) {
                        "0${hour}:0${minute}"
                    } else {
                        "0${hour}:${minute}"
                    }

                }

                else -> {
                    if (minute < 10) {
                        "${hour}:0${minute}"
                    } else {
                        "${hour}:${minute}"
                    }
                }
            }
        }

        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    //  앨범 에서 사진 가져오기위한  사전작업 시작
    @SuppressLint("IntentReset")
    fun bt1(view: View?) {    // 사진등록 버튼을 누르면 실행됨 이미지 고를 갤러리 오픈
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "load image"), PERMISSION_GALLERY)


    }

    @Deprecated("Deprecated in Java")
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_GALLERY) {
            var dataUri = data?.data
            data2 = dataUri.toString()
            ModimageURl = Uri.parse(data2)

            try {

                Glide.with(this).load(dataUri).override(100, 100).into(binding.detailRsImage)

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "${e.message} 실패하셨습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        if (requestCode == CAMERA_CODE) {
            if (data?.extras?.get("data") != null) {
                val img = data.extras?.get("data") as Bitmap
                val uri = saveFile(Date().toString(), "image/jpg", img)
                ModimageURl = Uri.parse(uri.toString())
                binding.detailRsImage.setImageURI(uri)
            }
        }
    }

    private fun showImageSelectDialog() {
        // 다이얼로그
        val title = "카메라 앨범 중 하나를 선택 해주세요."

        val dialog = ModImageDialog(this@RestaurantDetailActivityMod, title, null, "카메라", 0)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "ConfirmDialog")
    }

    // 카메라 촬영 - 권한 처리
    private fun takePhoto() {
        val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(itt, CAMERA_CODE)
    }

    @SuppressLint("Recycle")
    fun saveFile(fileName: String, mimeType: String, bitmap: Bitmap): Uri? {

        val CV = ContentValues()

        // MediaStore 에 파일명, mimeType 을 지정
        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        // 안정성 검사
        CV.put(MediaStore.Images.Media.IS_PENDING, 1)

        // MediaStore 에 파일을 저장
        // requireContext().contentResolver 이게 뭘까??
        val uri = applicationContext.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            CV
        )
        Log.i("uri", uri.toString())

        if (uri != null) {
            val descriptor = applicationContext.contentResolver.openFileDescriptor(uri, "w")

            val fos = FileOutputStream(descriptor?.fileDescriptor)

            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, fos)
            fos.close()

            CV.clear()
            // IS_PENDING 을 초기화
            CV.put(MediaStore.Images.Media.IS_PENDING, 0)
            applicationContext.contentResolver.update(uri, CV, null, null)
        }
        return uri
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this@RestaurantDetailActivityMod,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ),
                1000
            )
        } else {
            ActivityCompat.requestPermissions(
                this@RestaurantDetailActivityMod,
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ),
                1000
            )
        }

    }

    override fun onClickImageYesButton(id: Int) {
        takePhoto()
    }

    override fun onClickImageNoButton(id: Int) {
        bt1(binding.detailRsImage)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            showImageSelectDialog()
        } else {
            showPermissionDialog()
        }
    }

    private fun showPermissionDialog() {
        val dialog = ModRePermissionDialog(this@RestaurantDetailActivityMod, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "permission")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun modpermissionYesButton(id: Int) {
        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
            requestPermission()
        }
    }

    override fun modpermissionNoButton(id: Int) {
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    // 맵을 크개보여주는 다이얼로그 호출
    private fun ModMapsDialogCome(lat: Double, long: Double) {
        val dialog = ModMapsDialog(this@RestaurantDetailActivityMod, lat, long, 0)
        dialog.isCancelable = false
        dialog.show(supportFragmentManager, "Maps")
    }

    override fun onClickMapYesButton(id: Int, latitude3: Double, longitude3: Double) {
        lat = latitude3
        long = longitude3
        Log.d("latlong!!!!!", "${lat}, ${long}")
        setMarker()

        mapView.getMapAsync(this)
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
                    ModBottomNaviDialogCome("메인페이지")
                    true
                }

                R.id.naviSearch -> {
                    ModBottomNaviDialogCome("지도페이지")
                    true
                }

                R.id.naviFavorite -> {
                    ModBottomNaviDialogCome("좋아요페이지")
                    true
                }

                R.id.naviAdd -> {
                    ModBottomNaviDialogCome("등록페이지")
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

    private fun ModBottomNaviDialogCome(title: String) {
        val dialog = ModBottomNaviDialog(this@RestaurantDetailActivityMod, title, 0)
        dialog.isCancelable
        dialog.show(supportFragmentManager, "BottomNavi")
    }

    override fun naviYesClickButton(id: Int, tag: String) {
        when (tag) {
            "메인페이지" -> {
                goToMainActivity()
            }

            "지도페이지" -> {
                goToMapsActivity()
            }

            "좋아요페이지" -> {
                goToBookmarkActivity()
            }

            "등록페이지" -> {
                goToAddRestaurantDataActivity()
            }
        }
    }
}