package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogCategoryBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

interface ModCategoryDialogInterface {
    fun onClickCategoryYesButton(id: Int)

}

class ModCategoryDialog(modCategoryDialogInterface: RestaurantDetailActivityMod, id: Int) : DialogFragment() {
    private var _binding: DialogCategoryBinding? = null
    private val binding get() = _binding!!
    private var modCategoryDialogInterface: ModCategoryDialogInterface? = null
    private var id: Int? = null
    private var lastSelectedChip: Chip? = null
    var selectedCategory: String? = null

    init {
        this.id = id
        this.modCategoryDialogInterface = modCategoryDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // 취소 버튼이 없는 다이얼로그는 id를 -1로 넘겨줌
        if (id == -1) {
            // 취소 버튼을 보이지 않게 처리
            binding.dialogNoBtn.visibility = View.GONE
        }
        val chipGroup = binding.mainCategoryGroup
        // 메인 엑티비티에 접속해서 메인에있는 함수 호출하기

        chipGroup.setOnCheckedStateChangeListener {
                group, checkedIds ->
            handleChipGroupClick( group, checkedIds)
            (activity as RestaurantDetailActivityMod).receiveCategory(selectedCategory.toString())
        }

        // 취소 버튼 클릭
        binding.dialogNoBtn.setOnClickListener {
            dismiss()
        }
        // 확인 버튼 클릭
        binding.dialogYesBtn.setOnClickListener {

            this.modCategoryDialogInterface?.onClickCategoryYesButton(id!!)
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun handleChipGroupClick(

        chipGroup: ChipGroup,
        checkedIds: List<Int>
    ) {
        val chip: Chip? = checkedIds.firstOrNull()?.let { chipGroup.findViewById(it) }
        chip?.let {

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
            selectedCategory = chip.text.toString()
        }
    }

}