package com.example.restaurantreview.main.helper

import android.util.Log
import com.example.restaurantreview.databinding.FragmentAddRestaurantDataBinding
import com.example.restaurantreview.main.view.AddRestaurantDataActivity
import com.example.restaurantreview.main.view.AddRestaurantDataFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

object AddCategoryHelper {
    private var lastSelectedChip: Chip? = null
    var selectedCategory: String? = null
    /**
     * mainCategory 설정하는 함수
     *
     * @param context 애플리케이션의 컨텍스트.
     * @param binding 액티비티의 바인딩 객체.
     *
     * @JavaConvert chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
     *     @Override
     *     public void onCheckedStateChanged(ChipGroup group, List<Integer> checkedIds) {
     *         // Your code here
     *     }
     * });
     */
    fun setupCategory(context: AddRestaurantDataFragment, binding: FragmentAddRestaurantDataBinding) {
        // mainCategory 클릭 리스너 설정
        val chipGroup = binding.mainCategoryGroup
        // 메인 엑티비티에 접속해서 메인에있는 함수 호출하기
        val activity = binding.root.context as AddRestaurantDataActivity


        // 칩 그룹에 속한 칩이 변경될 때 호출될 콜백을 등록.
        // 이 콜백은 single selection mode 에서만 호출된다.
        chipGroup.setOnCheckedStateChangeListener {
            // ChipGroup.OnCheckedStateChangeListener 익명 함수가 오버라이드 된 것
            // onCheckedStateChanged 메서드를 람다로 구현
            // https://developer.android.com/reference/com/google/android/material/chip/ChipGroup.OnCheckedStateChangeListener#summary
                group, checkedIds ->
            handleChipGroupClick(context, group, checkedIds)
            // 메인에 변한 값 계속 보내기
            selectedCategory?.let { activity.receiveData(it) }

            // 테스트 코드
            Log.d("chipGroup.setOnCheckedStateChangeListener", "group : $group  IDX : $chipGroup}")
        }


    }

    fun handleChipGroupClick(
        context: AddRestaurantDataFragment,
        chipGroup: ChipGroup,
        checkedIds: List<Int>
    ) {
        val chip: Chip? = checkedIds.firstOrNull()?.let { chipGroup.findViewById(it) }

        chip?.let {

            // 테스트Code 토스트 메시지 출력
//            Toast.makeText(context, it.text, Toast.LENGTH_SHORT).show()

            // 체크 상태는 한개만 존재하도록 lastSelectedChip 으로 상태 관리
            //
            if (lastSelectedChip == it) {
                it.isChecked = false
                lastSelectedChip = null
            } else {
                // 이전 선택된 Chip의 선택 해제
                lastSelectedChip?.isChecked = false
                // 현재 선택된 Chip의 선택 설정
                it.isChecked = true
                // 마지막 선택된 Chip 업데이트
                lastSelectedChip = it
            }

            // 테스트용 (idx가 알아볼수 없어서 테스트
            val chipIndex = getChipIndex(chipGroup, it.id)
            Log.i("SelectedChipIndex", chipGroup.checkedChipId.toString())
//            Toast.makeText(context, chipGroup.checkedChipId.toString(), Toast.LENGTH_SHORT).show()
            selectedCategory = chip.text.toString()

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