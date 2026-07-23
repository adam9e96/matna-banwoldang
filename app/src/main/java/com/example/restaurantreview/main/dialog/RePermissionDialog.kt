package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogRepermissionBinding


interface RePermissionDialogInterface{
    fun permissionYesButton(id:Int)
    fun permissionNoButton(id:Int)
}
class RePermissionDialog(rePermissionDialogInterface: RePermissionDialogInterface, id: Int): DialogFragment() {
    private var _binding: DialogRepermissionBinding? = null
    private val binding get() = _binding!!
    private var id: Int? = null
    private var rePermissionDialogInterface: RePermissionDialogInterface?= null
    init {
        this.rePermissionDialogInterface = rePermissionDialogInterface
        this.id = id
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogRepermissionBinding.inflate(inflater, container, false)
        val view = binding.root
        binding.dialogTitleTv.text = "권한을 허용해 주세요"
        binding.dialogDescTv.text = "권한 허용을 안할시 앱이 종료 됩니다."
        // 취소 버튼 클릭
        binding.dialogNoBtn.setOnClickListener {
            this.rePermissionDialogInterface?.permissionNoButton(id!!)
            dismiss()
        }
        //확인 버튼 클릭
        binding.dialogYesBtn.setOnClickListener {
            this.rePermissionDialogInterface?.permissionYesButton(id!!)
            dismiss()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}