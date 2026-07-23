package com.example.restaurantreview.main.settings

import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import com.example.restaurantreview.R
import com.example.restaurantreview.databinding.ToastCustomBinding

object CustomToast {

    fun createToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val binding: ToastCustomBinding =
            DataBindingUtil.inflate(inflater, R.layout.toast_custom, null, false)

        binding.tvSample.text = message

        // 애니메이션 적용
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_top)
        binding.root.startAnimation(animation)

        val dialog = Dialog(context, R.style.CustomToastDialog)
        dialog.setContentView(binding.root)

        val window = dialog.window
        if (window != null) {
            val layoutParams = window.attributes
            layoutParams.gravity = Gravity.CENTER
            window.attributes = layoutParams
            window.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()

        // 지정된 시간 후에 다이얼로그를 닫습니다.
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 1000) // 1초 동안 표시
    }
}
