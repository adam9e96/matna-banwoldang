package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogNullcheckedBinding
import com.example.restaurantreview.main.view.AddRestaurantDataActivity

interface AddCheckedDialogInterface {
    fun onClickCheckedYesButton(id: Int,content: String?)
}

class AddCheckedDialog(
    addCheckedDialogInterface: AddRestaurantDataActivity,
    content: String?,
    id: Int
) : DialogFragment() {
    private var _binding: DialogNullcheckedBinding? = null
    private val binding get() = _binding!!
    private var addCheckedDialogInterface: AddCheckedDialogInterface? = null
    private var content: String? = null
    private var id: Int? = null

    init {
        this.addCheckedDialogInterface = addCheckedDialogInterface
        this.content = content
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNullcheckedBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.dialogTitleTv.text = "빈칸을 채워 주세요."
        binding.dialogDescTv.text = content

        binding.dialogYesBtn.setOnClickListener {
            addCheckedDialogInterface?.onClickCheckedYesButton(id!!,content)
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}