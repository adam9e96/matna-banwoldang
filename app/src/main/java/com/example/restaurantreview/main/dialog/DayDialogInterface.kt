package com.example.restaurantreview.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.restaurantreview.databinding.DialogDayBinding
import com.example.restaurantreview.main.view.RestaurantDetailActivityMod


interface DayDialogInterface {
    fun onClickYesButton(id: Int)
}

class DayDialog(dayDialogInterface: RestaurantDetailActivityMod, id: Int) : DialogFragment() {
    private var _binding: DialogDayBinding? = null
    val binding get() = _binding!!
    private var dayDialogInterface: DayDialogInterface? = null
    private var id: Int? = null
    val muList = mutableListOf<String>()

    init {
        this.id = id
        this.dayDialogInterface = dayDialogInterface
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDayBinding.inflate(inflater, container, false)
        val view = binding.root


        // 취소 버튼이 없는 다이얼로그는 id를 -1로 넘겨줌
        if (id == -1) {
            // 취소 버튼을 보이지 않게 처리
            binding.dialogNoBtn.visibility = View.GONE
        }


        // 취소 버튼 클릭
        binding.dialogNoBtn.setOnClickListener {
            dismiss()
        }
        // 확인 버튼 클릭
        binding.dialogYesBtn.setOnClickListener {
            binding.run {
                if (monday.isChecked) {
                    muList.add(monday.text.toString())
                }
                if (tuesday.isChecked) {
                    muList.add(tuesday.text.toString())
                }
                if (wednesday.isChecked) {
                    muList.add(wednesday.text.toString())
                }
                if (thursday.isChecked) {
                    muList.add(thursday.text.toString())
                }
                if (friday.isChecked) {
                    muList.add(friday.text.toString())
                }
                if (saturday.isChecked) {
                    muList.add(saturday.text.toString())
                }
                if (Sunday.isChecked) {
                    muList.add(Sunday.text.toString())
                }
            }
            (activity as RestaurantDetailActivityMod).receiveDayList(muList.toString().replace("[",""))
            this.dayDialogInterface?.onClickYesButton(id!!)

            dismiss()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}