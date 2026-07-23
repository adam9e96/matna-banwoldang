package com.example.restaurantreview.main.helper

import android.util.Log
import android.view.View
import com.example.restaurantreview.databinding.FragmentAddRestaurantDataBinding
import com.example.restaurantreview.main.view.AddRestaurantDataActivity
import com.example.restaurantreview.main.view.AddRestaurantDataFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object AddTagHelper {
    private var isExpanded = false

    val list = mutableListOf<String>()
    /**
     * mainCategory 설정하는 함수
     *
     * @param context 애플리케이션의 컨텍스트.
     * @param binding 액티비티의 바인딩 객체.
     */
    fun setupCategory(context: AddRestaurantDataFragment, binding: FragmentAddRestaurantDataBinding) {
        // mainCategory 클릭 리스너 설정
        val chipGroup = binding.mainTagGroup
        val showMoreButton = binding.mainTagShowButton
        // 메인 엑티비티에 접속해서 메인에있는 함수 호출하기
        val activity = binding.root.context as AddRestaurantDataActivity
        // 기본적으로 첫 5개 칩만 보이게 설정
        val initialVisibleChips = 5
        for (i in initialVisibleChips until chipGroup.childCount) {
            chipGroup.getChildAt(i).visibility = View.GONE
        }

        // "더 보기" 버튼 클릭 리스너 설정
        showMoreButton.setOnClickListener {
            if (isExpanded) {
                // 칩을 다시 숨기기
                for (i in initialVisibleChips until chipGroup.childCount) {
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
            isExpanded = !isExpanded
        }

        // 칩 그룹에 속한 칩이 변경될 때 호출될 콜백을 등록
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            list.clear()
            handleChipGroupClick(context, group, checkedIds)
            // 메인에 있는 메서드 호출
            activity.receiveList(list)
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

                // 테스트용 (인덱스를 알아보기 위해)
                val chipIndex = getChipIndex(chipGroup, it.id)
                // 테스트용 chip.text.toString() 이 내가 원하는 값인지 확인(결과 맞음)
                Log.i("SelectedChipIndex", "$chipIndex ,${chip.text.toString()}")
                // 리스트 비우고 클릭 되어있는 태그 리스트에 추가하기
                list.add(chip.text.toString())
                Log.i("listTest", list.toString())
            }
        }
    }

    private fun getChipIndex(chipGroup: ChipGroup, chipId: Int): Int {
        for (i in 0 until chipGroup.childCount) {
            if (chipGroup.getChildAt(i).id == chipId) {
                return i
            }
        }
        return -1 // Chip을 찾지 못한 경우
    }
}