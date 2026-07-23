package com.example.restaurantreview.main.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.graphics.scale
import com.elvishew.xlog.XLog
import com.example.restaurantreview.main.model.Restaurant
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Firebase 실시간 데이터베이스(Realtime Database)에서 레스토랑(맛집) 데이터를 관리하는 클래스
 *
 * 주요 기능:
 * - restaurants 노드에서 가장 마지막 레스토랑 인덱스를 가져오기
 * - 레스토랑 데이터 추가 시 가장 마지막 레스토랑 +1을 한 다음 추가하기
 * - 특정 인덱스의 데이터를 가져오기
 * - 태그로 데이터 조회하기
 * - 이미지 로드 및 삭제하기
 * - 특정 ID의 레스토랑 데이터 업데이트 및 삭제하기
 * - 북마크된 레스토랑 데이터만 조회하기
 *
 * 이 클래스는 Firebase Realtime Database와 Storage를 사용하여 레스토랑 데이터를 추가, 조회, 업데이트, 삭제하는
 * 주요 기능들을 제공합니다.
 *
 * 비동기적으로 데이터를 처리하며, 데이터 로드와 업로드 성공 및 실패 시 로그를 기록하고 토스트 메시지를 표시합니다.
 */
class FirebaseManager(private val context: Context) {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("restaurants")


    init {
        // 'restaurants' 노드의 데이터를 항상 동기화
        database.keepSynced(true)
    }

    /**
     * 'restaurants' 노드 아래에 새로운 레스토랑(맛집)을 데이터베이스에 추가
     * 마지막 인덱스를 찾아서 자동으로 증가시켜 고유 ID를 보장하면서 추가된 순서로 저장
     *
     * @param restaurant 데이터베이스에 추가할 Restaurant 객체
     * @author fpkm9999
     */
    fun addRestaurant(restaurant: Restaurant) {
        database.orderByKey().limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastId = snapshot.children.lastOrNull()?.key?.toIntOrNull() ?: 0
                    val newId = lastId + 1
                    restaurant.id = newId.toString() // newId 값을 restaurant.id에 할당
                    database.child(newId.toString()).setValue(restaurant)
                    XLog.d("addRestaurant() - $newId 번 노드에 레스토랑 추가 성공!")
                }

                override fun onCancelled(error: DatabaseError) {
                    XLog.e("addRestaurant() - 데이터베이스 추가 오류: ${error.message}")
                }
            })
    }

    /**
     * Firebase 실시간 데이터베이스에서 레스토랑 데이터를 가져와서 콜백 함수를 통해 반환
     * 비동기적으로 데이터를 로드하며, 데이터가 성공적으로 로드되면 콜백 함수가 호출되어 로드된 레스토랑 리스트를 반환
     *
     * @param callback 레스토랑 리스트를 반환하는 콜백 함수
     * @author fpkm9999
     */
    fun loadRestaurantData(callback: (List<Restaurant>) -> Unit) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                XLog.d("loadRestaurantData() - 파이어베이스에서 데이터 로드 시작!")

                // 스냅샷을 Restaurant 객체로 변환
                val restaurants = snapshot.children.mapNotNull { child ->
                    val restaurant = createRestaurantFromSnapshot(child)
                    if (restaurant != null) {
                        // 테스트 코드
                        // XLog.d("loadRestaurantData() - 레스토랑 데이터를 객체로 변환 성공: $restaurant")
                    } else {
                        XLog.d("loadRestaurantData() - 레스토랑 데이터 변환 실패")
                    }
                    restaurant
                }

                if (restaurants.isNotEmpty()) {
                    XLog.d("loadRestaurantData() - 로드된 레스토랑 수: ${restaurants.size}")

                    val formattedRestaurants =
                        restaurants.joinToString(separator = "\n") { restaurant ->
                            restaurant.toString()
                        }
                    XLog.d("loadRestaurantData() - 로드된 레스토랑:\n$formattedRestaurants")
                } else {
                    XLog.d("loadRestaurantData() - 로드된 레스토랑이 없습니다.")
                }
                callback(restaurants) // 반환된 레스토랑 리스트를 콜백 함수에 전달
            }

            override fun onCancelled(error: DatabaseError) {
                XLog.e("loadRestaurantData() - 데이터 로드 실패: ${error.message}")
                Toast.makeText(context, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Firebase에서 가져온 DataSnapshot 객체를 Restaurant 객체로 변환하는 메서드
     *
     * @param snapshot Firebase DataSnapshot
     * @return 변환된 Restaurant 객체 또는 변환 실패 시 null
     */
    private fun createRestaurantFromSnapshot(snapshot: DataSnapshot): Restaurant? {
        return try {
            val store = snapshot.child("store").getValue(String::class.java) ?: ""
            val menu = snapshot.child("menu").getValue(String::class.java) ?: ""
            val review = snapshot.child("review").getValue(String::class.java)
            val rating = snapshot.child("rating").getValue(Float::class.java) ?: 0.0f
            val latitude = snapshot.child("latitude").getValue(Double::class.java) ?: 0.0
            val longitude = snapshot.child("longitude").getValue(Double::class.java) ?: 0.0
            val tags =
                snapshot.child("tags").children.mapNotNull { it.getValue(String::class.java) }
            val businessDays = snapshot.child("businessDays").getValue(String::class.java) ?: ""
            val businessHours = snapshot.child("businessHours").getValue(String::class.java) ?: ""
            val breakTime = snapshot.child("breakTime").getValue(String::class.java)
            val bookmark = when (val bookmarkValue = snapshot.child("bookmark").value) {
                is Boolean -> bookmarkValue
                is Long -> bookmarkValue == 1L
                is Double -> bookmarkValue == 1.0
                else -> false
            }
            val category = snapshot.child("category").getValue(String::class.java) ?: ""
            val imageURl = snapshot.child("imageURl").getValue(String::class.java) ?: ""
            val reviewDate = snapshot.child("reviewDate").getValue(String::class.java) ?: ""
            val id = snapshot.key ?: ""

            Restaurant(
                id = id,
                store = store,
                menu = menu,
                review = review,
                rating = rating,
                latitude = latitude,
                longitude = longitude,
                tags = tags,
                businessDays = businessDays,
                businessHours = businessHours,
                breakTime = breakTime,
                bookmark = bookmark,
                category = category,
                imageURl = imageURl,
                reviewDate = reviewDate
            )
        } catch (e: Exception) {
            XLog.e("createRestaurantFromSnapshot() - 스냅샷을 Restaurant 객체로 변환 실패: ${e.message}")
            null
        }
    }


    /**
     * 검색어를 통해 가게 이름, 카테고리, 태그를 조회하는 함수
     *
     * 이 함수는 주어진 검색어를 사용하여 Firebase Realtime Database에서 레스토랑 데이터를 필터링하여 조회합니다.
     * 검색어가 가게 이름, 카테고리, 태그에 포함되어 있는지 조사하여 일치하는 레스토랑 데이터를 반환합니다.
     *
     * @param query 검색어
     * @param callback 결과를 반환할 콜백 함수
     * @author fpkm9999
     */
    fun searchRestaurants(
        query: String,
        callback: (List<Restaurant>) -> Unit
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 모든 데이터를 가져옴
                val restaurants = snapshot.children.mapNotNull { child ->
                    createRestaurantFromSnapshot(child)
                }

                // 검색어가 가게 이름, 카테고리, 태그에 포함되어 있는지 조사하여 필터링
                val filterRestaurants = restaurants.filter { restaurant ->
                    restaurant.store.contains(query, true) ||
                            restaurant.category.contains(query, true) ||
                            restaurant.tags.any { it.contains(query, true) }
                }

                XLog.d("searchRestaurants() - 검색된 맛집 수: ${filterRestaurants.size}")
                XLog.d("searchRestaurants() - 검색된 맛집 목록: $filterRestaurants")

                callback(filterRestaurants)
            }

            override fun onCancelled(error: DatabaseError) {
                XLog.e("searchRestaurants() - 데이터 로드 실패: ${error.message}")
                Toast.makeText(context, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * 카테고리와 태그를 기준으로 레스토랑 데이터를 조회하는 함수
     *
     * 이 함수는 주어진 카테고리와 태그 리스트를 기준으로 Firebase Realtime Database에서
     * 레스토랑 데이터를 필터링하여 조회합니다.
     * 카테고리와 태그 중 하나만 선택할 수도 있으며, 둘 다 선택하지 않으면 모든 레스토랑 데이터를 반환합니다.
     *
     * @param category 조회할 카테고리 (선택 사항)
     * @param tags 조회할 태그 리스트 (선택 사항)
     * @param callback 조회된 레스토랑 리스트를 반환하는 콜백 함수
     * @author fpkm9999
     */
    fun getRestaurantsByCategoryAndTags(
        category: String?,
        tags: List<String>?,
        callback: (List<Restaurant>) -> Unit
    ) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val restaurants = snapshot.children.mapNotNull { child ->
                    val restaurant = createRestaurantFromSnapshot(child)
                    if (restaurant != null) {
                        val categoryMatch = category?.let { restaurant.category == it } ?: true
                        val tagsMatch = tags?.all { it in restaurant.tags } ?: true

                        if (categoryMatch && tagsMatch) {
                            restaurant
                        } else {
                            null // 조건에 맞지 않는 경우 null 반환
                        }
                    } else {
                        null // 스냅샷이 null인 경우 null 반환
                    }
                }

                val formattedRestaurants =
                    restaurants.joinToString(separator = "\n") { restaurant ->
                        restaurant.toString()
                    }
                XLog.d("getRestaurantsByCategoryAndTags() - 검색된 맛집 수: ${restaurants.size}")
                XLog.d("getRestaurantsByCategoryAndTags() - 검색된 맛집: $formattedRestaurants")
                callback(restaurants)
            }

            override fun onCancelled(error: DatabaseError) {
                XLog.e("getRestaurantsByCategoryAndTags() - 데이터 로드 실패: ${error.message}")
                Toast.makeText(context, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    /**
     * 이미지 URL을 가져와서 webp로 변환하고 Firebase에 업로드하는 함수
     *
     * 예제 이미지 URI (실제 앱에서는 갤러리나 카메라에서 선택된 URI를 사용):
     * val imageUri = Uri.parse("content://path/to/your/image.jpg")
     * val imageName = "example_image"
     *
     * uploadImageToFirebase(this, imageUri, imageName) { success, downloadUrl ->
     *     if (success) {
     *         Toast.makeText(this, "Image uploaded successfully: $downloadUrl", Toast.LENGTH_LONG).show()
     *     } else {
     *         Toast.makeText(this, "Image upload failed.", Toast.LENGTH_LONG).show()
     *     }
     * }
     *
     * @param context 컨텍스트 객체
     * @param imageUri 업로드할 이미지의 URI
     * @param imageName 업로드할 이미지의 이름
     * @param callback 업로드 결과를 반환하는 콜백 함수
     * @author fpkm9999
     */
    fun uploadImageToFirebase(
        context: Context,
        imageUri: Uri,
        imageName: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val storageRef = FirebaseStorage.getInstance().reference

        // 확장자가 webp 검사
        val isWebp = imageUri.toString().endsWith("webp", ignoreCase = true)
        val time: String =
            SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val fileRef = storageRef.child("images/${imageName}_${time}.webp")
        XLog.d("uploadImageToFirebase() : 저장될 파일 경로 - $fileRef")

        if (isWebp) {
            // 이미지가 이미 webp 형식인 경우 변환 없이 업로드
            XLog.d("uploadImageToFirebase() - 이미지가 이미 webp 형식.")
            uploadFileToFirebase(fileRef, imageUri, callback) // 파일 업로드
        } else {
            // 이미지가 png 형식이거나 다른 형식인 경우 webp로 변환 후 업로드
            XLog.d("uploadImageToFirebase() - 이미지를 webp 형식으로 변환 중...")
            val webpUri = convertImageToWebp(context, imageUri)
            if (webpUri != null) {
                uploadFileToFirebase(fileRef, webpUri, callback) // 파일 업로드
            } else {
                // 이미지 형식이 webp 가 아니고 webp 변환 결과가 null 인 경우
                XLog.e("uploadImageToFirebase() - 이미지 변환 실패")
                callback(false, null)
            }
        }
    }

    /**
     * 이미지를 webp로 변환하는 함수
     * 이미지 Uri를 Bitmap으로 로드하고 webp 형식으로 변환(인코딩)한 후, 캐시 디렉토리에 저장
     * 캐시 디렉토리에 저장된 파일의 Uri를 반환
     *
     * @param context 컨텍스트 객체
     * @param imageUri 변환할 이미지의 Uri
     * @return 변환된 파일의 Uri
     * @author fpkm9999
     */
    private fun convertImageToWebp(context: Context, imageUri: Uri): Uri? {
        return try {
            // 이미지 파일을 Bitmap으로 로드
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream) // 비트맵으로 변환
            inputStream?.close()
            XLog.d("convertImageToWebp() - Bitmap 로드 성공")

            // 변환된 이미지 파일을 저장할 경로 설정 (임시 파일제목.webp)
            val file = File(context.cacheDir, "converted_image.webp")

            FileOutputStream(file).use { outputStream ->
                bitmap.scale(500, 500)
                    .compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 100, outputStream)
            }
            XLog.d("convertImageToWebp() - Bitmap 변환 및 저장 성공")
            XLog.d("convertImageToWebp() - $file")

            // 변환된 파일을 Uri 반환
            Uri.fromFile(file)
        } catch (e: Exception) {
            XLog.e("convertImageToWebp() - 이미지 변환 중 오류 발생: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    /**
     * Firebase Storage에 파일을 업로드하는 함수
     *
     * 이 메서드는 주어진 파일을 Firebase Storage에 업로드하고, 업로드가 성공하면 해당 파일의 다운로드 URL을 콜백으로 반환합니다.
     * 업로드 또는 URL 가져오기가 실패하면 콜백에 false와 null을 반환합니다.
     *
     * @param fileRef 업로드할 파일의 Firebase Storage 참조
     * @param fileUri 업로드할 파일의 URI
     * @param callback 업로드 결과와 다운로드 URL을 반환하는 콜백 함수
     * @author fpkm9999
     */
    private fun uploadFileToFirebase(
        fileRef: StorageReference,
        fileUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        fileRef.putFile(fileUri).addOnSuccessListener {
            XLog.d("uploadFileToFirebase() - 파일 업로드 성공: $fileUri")

            // 업로드 성공 시 다운로드 URL 가져오기
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                XLog.d("uploadFileToFirebase() - 다운로드 URL 가져오기 성공: $uri")
                callback(true, uri.toString())
            }.addOnFailureListener { exception ->
                XLog.e("uploadFileToFirebase() - 다운로드 URL 가져오기 실패: ${exception.message}")
                callback(false, null)
            }
        }.addOnFailureListener { exception ->
            XLog.e("uploadFileToFirebase() - 파일 업로드 실패: ${exception.message}")
            callback(false, null)
        }
    }


    /**
     * Firebase Realtime Database에서 특정 ID를 가진 레스토랑 데이터를 업데이트하는 함수
     *
     * 이 함수는 주어진 ID를 사용하여 Firebase Realtime Database에서 해당 레스토랑 데이터를 업데이트합니다.
     * 데이터 업데이트가 성공하면 성공 메시지를 토스트로 표시하고, 실패하면 오류 메시지를 토스트로 표시합니다.
     *
     * @param id 업데이트할 레스토랑의 고유 ID
     * @param restaurant 업데이트할 레스토랑 객체
     * @author fpkm9999
     */
    fun updateRestaurant(id: String, restaurant: Restaurant) {
        XLog.d("updateRestaurant() - 업데이트 할 id : $id")

        database.child(id).setValue(restaurant)
            .addOnSuccessListener {
                Toast.makeText(context, "레스토랑 정보가 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                XLog.d("updateRestaurant() - 성공 : $restaurant")
            }
            .addOnFailureListener { error ->
                Toast.makeText(context, "데이터 업데이트 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                XLog.e("updateRestaurant() - 업데이트 실패 : ${error.message}")
            }
    }

    /**
     * Firebase Realtime Database에서 특정 ID를 가진 레스토랑 데이터를 비동기적으로 로드하는 함수
     *
     * 이 함수는 주어진 ID를 사용하여 Firebase Realtime Database에서 해당 레스토랑 데이터를 로드합니다.
     *
     * 데이터 로드가 성공하면 `레스토랑 객체`를 콜백 함수로 반환합니다.
     *
     * 데이터 로드가 실패하면 오류 메시지를 토스트로 표시하고 콜백 함수에 null을 전달합니다.
     *
     * @param id 로드할 레스토랑의 고유 ID
     * @param callback 로드된 레스토랑 객체를 반환하는 콜백 함수, 실패 시 null을 반환
     * @author fpkm9999
     */
    fun getRestaurantById(id: String, callback: (Restaurant?) -> Unit) {
        database.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val restaurant = createRestaurantFromSnapshot(snapshot)
                XLog.d("getRestaurantById() - $restaurant")
                callback(restaurant)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                XLog.e("getRestaurantById() - ${error.message}")
                callback(null)
            }
        })
    }

    /**
     * Firebase Realtime Database에서 특정 ID를 가진 레스토랑(맛집) 데이터를 삭제하는 함수
     *
     * 주어진 ID를 이용하여 Firebase Realtime Database에서 해당 레스토랑 데이터를 삭제합니다.
     * 삭제가 성공하면 성공 로그를 기록하고 콜백 함수에 true를 전달합니다.
     * 삭제가 실패하면 오류 로그를 기록하고 콜백 함수에 false를 전달합니다.
     *
     * @param id 삭제할 레스토랑의 고유 ID
     * @param callback 삭제 작업의 성공 여부를 반환하는 콜백 함수
     * @author fpkm9999
     *
     */
    fun deleteRestaurantById(id: String, callback: (Boolean) -> Unit) {
        database.child(id).removeValue().addOnSuccessListener {
            XLog.d("deleteRestaurantById() - 맛집 삭제 성공: $id 번 노드")
            callback(true)
        }.addOnFailureListener { exception ->
            XLog.e("deleteRestaurantById() - 맛집 삭제 실패: ${exception.message}")
            callback(false)
        }
    }


    /**
     * Firebase Storage에서 이미지 URL을 사용하여 이미지를 삭제하는 함수
     *
     * @param imageUrl 삭제할 이미지의 URL
     * @param callback 이미지 삭제 작업의 성공 여부를 반환하는 콜백 함수
     * @author fpkm9999
     */
    fun deleteImageByUrl(imageUrl: String, callback: (Boolean) -> Unit) {
        try {
            // 이미지 URL에서 파일 경로 추출
            val fileName = getFileNameFromUrl(imageUrl)

            // Firebase Storage 참조 생성
            val storageRef = FirebaseStorage.getInstance().reference.child(fileName)

            // 파일 삭제 실행
            storageRef.delete().addOnSuccessListener {
                XLog.d("deleteImageByUrl() - 이미지 삭제 성공: $imageUrl")
                callback(true)
            }.addOnFailureListener { exception ->
                XLog.e("deleteImageByUrl() - 이미지 삭제 실패: ${exception.message}")
                callback(false)
            }
        } catch (e: Exception) {
            XLog.e("deleteImageByUrl() - 이미지 삭제 중 오류 발생: ${e.message}")
            callback(false)
        }
    }

    /**
     * 주어진 URL에서 파일 이름을 추출하는 함수
     *
     * @author fpkm9999
     * @param url 파일 URL 문자열
     * @return 추출된 파일 이름 문자열
     * @throws IllegalArgumentException 잘못된 URL일 경우 발생
     */
    private fun getFileNameFromUrl(url: String): String {
        val uri = Uri.parse(url)
        XLog.d("getFileNameFromUrl : $uri")
        return uri.pathSegments.lastOrNull() ?: throw IllegalArgumentException("잘못된 image URL")
    }


    /**
     * bookmark가 true인 레스토랑(맛집) 데이터만 가져오는 메소드
     *
     * @param callback 북마크된 레스토랑 리스트를 반환하는 콜백 함수
     */
    fun loadBookmarkedRestaurants(callback: (List<Restaurant>) -> Unit) {
        database.orderByChild("bookmark").equalTo(true)
            .addListenerForSingleValueEvent(object :
                ValueEventListener { // bookmark에서 true인 경우만 가져오기
                override fun onDataChange(snapshot: DataSnapshot) {
                    val restaurants: List<Restaurant> = snapshot.children.mapNotNull { child ->
                        createRestaurantFromSnapshot(child)
                    }
                    if (restaurants.isNotEmpty()) {
                        XLog.d("loadBookmarkedRestaurants() - 북마크된 맛집 수: ${restaurants.size}")
                    } else {
                        XLog.d("loadBookmarkedRestaurants() - 로드된 레스토랑이 없습니다.")
                    }
                    callback(restaurants)
                }

                override fun onCancelled(error: DatabaseError) {
                    XLog.e("loadBookmarkedRestaurants() - 데이터 로드 실패: ${error.message}")
                    Toast.makeText(context, "데이터 로드 실패: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}