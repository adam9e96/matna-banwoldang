package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogTagBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

interface ModTagDialogInterface {
    fun onClickTagYesButton(id: Int)
}
class ModTagDialog(modTagDialogInterface: RestaurantDetailActivityMod, id: Int): DialogFragment() {
    private var _binding: DialogTagBinding? = null
    private val binding get() = _binding!!
    private var modTagDialogInterface: ModTagDialogInterface? = null
    private var id: Int? = null
    val list = mutableListOf<String>()
    init {
        this.modTagDialogInterface = modTagDialogInterface
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTagBinding.inflate(inflater, container, false)
        val view = binding.root
        val chipGroup = binding.mainTagGroup
        if (id == -1) {
            // 취소 버튼을 보이지 않게 처리
            binding.dialogNoBtn.visibility = View.GONE
        }
        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            list.clear()
            handleChipGroupClick( group, checkedIds)
            (activity as RestaurantDetailActivityMod).receiveTag(list)

        }
        binding.dialogYesBtn.setOnClickListener {
            this.modTagDialogInterface?.onClickTagYesButton(id!!)
            dismiss()
        }
        binding.dialogNoBtn.setOnClickListener {
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
        // 선택된 모든 칩을 반복 처리
        checkedIds.forEach { id ->
            val chip: Chip? = chipGroup.findViewById(id)
            chip?.let {
                list.add(chip.text.toString())
            }
        }
    }
}