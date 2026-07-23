package com.example.restaurantreview.main.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Restaurant 데이터 클래스는 Firebase에서 가져온 레스토랑 정보를 저장
 *
 * Parcelable 인터페이스를 상속하는 이유
 * : 객체를 직렬화하여, 액티비티, 프래그먼트, 서비스 등 간에 데이터를 쉽게 전달하기 위해서
 *
 *         case1) 현재 엑티비티에서 NextActivity 엑티비티로 데이터 전달 예시
 *         val restaurant = Restaurant(store = "Pizza Place", menu = "Pizza, Pasta", rating = 4.5f)
 *         val intent = Intent(this, NextActivity::class.java)
 *         intent.putExtra("restaurant", restaurant)
 *         startActivity(intent)
 *
 *         case2) NextActivity 액티비티에서 데이터 읽기
 *         // Intent에서 Restaurant 객체를 가져옴
 *         val restaurant: Restaurant? = intent.getParcelableExtra("restaurant")
 *         restaurant?.let {
 *         // Restaurant 객체 사용
 *         Log.d("NextActivity", "Restaurant: ${it.store}, ${it.menu}, ${it.rating}")
 *         }
 *
 * @property id 레스토랑 ID(고유한 Key) * 기본키는 아님
 * @property store 레스토랑 이름
 * @property menu 레스토랑 메뉴
 * @property review 레스토랑 리뷰
 * @property rating 레스토랑 평점
 * @property latitude 레스토랑 위치의 위도
 * @property longitude 레스토랑 위치의 경도
 * @property tags 레스토랑과 관련된 태그 목록(리스트 형태)
 * @property businessDays 레스토랑 영업일
 * @property businessHours 레스토랑 영업 시간
 * @property breakTime 레스토랑 휴식 시간
 * @property bookmark 레스토랑 북마크 여부
 * @property category 레스토랑 카테고리
 * @property imageURl 레스토랑 이미지 URL
 * @property reviewDate 레스토랑 리뷰 작성일
 */
data class Restaurant(
    var id: String = "", // 추가된 ID 필드
    val store: String = "",
    val menu: String = "",
    val review: String? = null,
    val rating: Float = 0.0f,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val tags: List<String> = listOf(),
    val businessDays: String = "",
    val businessHours: String = "",
    val breakTime: String? = null,
    var bookmark: Boolean = false,
    val category: String = "",
    val imageURl: String = "",
    val reviewDate: String = ""
) : Parcelable {

    /**
     * Parcel에서 객체를 생성하는 생성자
     */
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "", // ID 필드 읽기
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readFloat(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(),
        parcel.readBoolean(), // Boolean 값을 읽어옵니다.
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    /**
     * 객체를 Parcel에 작성하는 메소드
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id) // ID 필드 쓰기
        parcel.writeString(store)
        parcel.writeString(menu)
        parcel.writeString(review)
        parcel.writeFloat(rating)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeStringList(tags)
        parcel.writeString(businessDays)
        parcel.writeString(businessHours)
        parcel.writeString(breakTime)
        parcel.writeBoolean(bookmark) // API 29 이상부터 사용가능함
        parcel.writeString(category)
        parcel.writeString(imageURl)
        parcel.writeString(reviewDate)
    }

    /**
     * Parcel 설명 메소드
     */
    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        /**
         * Parcel에서 Restaurant 객체를 생성하는 메소드
         */
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        /**
         * Restaurant 객체 배열을 생성하는 메소드
         */
        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }
}
