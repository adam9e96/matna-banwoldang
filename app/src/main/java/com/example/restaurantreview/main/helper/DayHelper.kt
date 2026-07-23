package com.example.restaurantreview.main.helper

import android.util.Log
import com.example.restaurantreview.databinding.FragmentAddRestaurantDataBinding
import com.example.restaurantreview.main.view.AddRestaurantDataActivity
import com.example.restaurantreview.main.view.AddRestaurantDataFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object DayHelper {

    val list = mutableListOf<String>()
    /**
     * mainCategory 설정하는 함수
     *
     * @param context 애플리케이션의 컨텍스트.
     * @param binding 액티비티의 바인딩 객체.
     */
    fun setupCategory(context: AddRestaurantDataFragment, binding: FragmentAddRestaurantDataBinding) {
        // gropup1(요일 그룹) 클릭 리스너 설정
        val chipGroup = binding.gropup1
        // 메인 엑티비티에 접속해서 메인에있는 함수 호출하기
        val activity = binding.root.context as AddRestaurantDataActivity
        // 칩 그룹에 속한 칩이 변경될 때 호출될 콜백을 등록
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            list.clear()
            handleChipGroupClick(context, group, checkedIds)
            // 메인에 있는 메서드 호출
            activity.receiveDayList(list)
        }
    }

    private fun handleChipGroupClick(
        context: AddRestaurantDataFragment,
        chipGroup: ChipGroup,
        checkedIds: List<Int>
    ) {
        // 선택된 모든 칩을 반복 처리
        checkedIds.forEach { id ->
            val chip: Chip? = chipGroup.findViewById(id)
            chip?.let {
                // 테스트용 토스트 메시지 출력
//                Toast.makeText(context, it.text, Toast.LENGTH_SHORT).show()


                // 테스트용 chip.text.toString() 이 내가 원하는 값인지 확인(결과 맞음)
                Log.i("SelectedChipIndex", chip.text.toString())
                // 리스트 비우고 클릭 되어있는 태그 리스트에 추가하기
                list.add(chip.text.toString())
                Log.i("listTest", list.toString())
            }
        }
    }

}