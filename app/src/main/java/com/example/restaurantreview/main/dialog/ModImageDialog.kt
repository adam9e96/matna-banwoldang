package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogCamaraBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod

interface ModImageDialogInterface {
    fun onClickImageYesButton(id: Int)
    fun onClickImageNoButton(id: Int)

}
class ModImageDialog(modImageDialogInterface: RestaurantDetailActivityMod, title: String, content: String?, buttonText: String, id: Int): DialogFragment() {
    // 뷰 바인딩 정의
    private var _binding: DialogCamaraBinding? = null
    private val binding get() = _binding!!
    private var modImageDialogInterface: ModImageDialogInterface? = null
    private var title: String? = null
    private var content: String? = null
    private var buttonText: String? = null
    private var id: Int? = null
    init {
        this.modImageDialogInterface = modImageDialogInterface
        this.title = title
        this.content = content
        this.buttonText = buttonText
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogCamaraBinding.inflate(inflater, container, false)
        val view = binding.root

        // 제목
        binding.dialogTitleTv.text = title

        // 확인 버튼 텍스트
        binding.dialogYesBtn.text = buttonText

        // 취소 버튼이 없는 다이얼로그는 id를 -1로 넘겨줌
        if (id == -1) {
            // 취소 버튼을 보이지 않게 처리
            binding.dialogNoBtn.visibility = View.GONE
        }

        // 카메라 버튼 클릭
        binding.dialogNoBtn.setOnClickListener {
            this.modImageDialogInterface?.onClickImageNoButton(id!!)
            dismiss()
        }

        // 앨범 버튼 클릭
        binding.dialogYesBtn.setOnClickListener {
            this.modImageDialogInterface?.onClickImageYesButton(id!!)
            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
