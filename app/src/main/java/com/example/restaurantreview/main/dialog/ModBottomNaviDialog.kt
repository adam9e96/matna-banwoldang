package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogCancelBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod

interface ModBottomNaviDialogInterface{
    fun naviYesClickButton(id: Int,tag: String)
}
class ModBottomNaviDialog(
    modBottomNaviDialogInterface: RestaurantDetailActivityMod, title: String, id: Int
) : DialogFragment() {
    private var _binding: DialogCancelBinding? = null
    private val binding get() = _binding!!
    private var modBottomNaviDialogInterface: ModBottomNaviDialogInterface? = null
    private var title: String? = null
    private var id: Int? = null
    init {
        this.title = title
        this.modBottomNaviDialogInterface = modBottomNaviDialogInterface
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCancelBinding.inflate(inflater, container, false)
        val view = binding.root
        // 제목
        binding.dialogTitleTv.text = "정말 ${title}로 가시겠습니까?"
        binding.dialogDescTv.text = "현재까지 작성하신 내용은 삭제됩니다."


        // 취소 버튼 클릭
        binding.dialogNoBtn.setOnClickListener {
            dismiss()
        }

        // 확인 버튼 클릭
        binding.dialogYesBtn.setOnClickListener {
            this.modBottomNaviDialogInterface?.naviYesClickButton(id!!,title!!)
            dismiss()
        }


        return view
    }
}