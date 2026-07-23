package com.example.restaurantreview.main.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.restaurantreview.databinding.FragmentAddRestaurantReviewBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddRestaurantReview.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRestaurantReview : Fragment() {
    lateinit var binding: FragmentAddRestaurantReviewBinding

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var text001: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRestaurantReviewBinding.inflate(inflater, container, false)
        binding.run {
//            test2.text = text001.toString()
//            Log.i("suummmm", text001.toString())
            val mActivity = activity as AddRestaurantDataActivity
            addRating.setOnRatingBarChangeListener { ratingBar, fl, b ->
                mActivity.receiveRating(ratingBar.rating.toFloat())
            }
            addMenu.addTextChangedListener {
                mActivity.receiveMenu(addMenu.text.toString())
            }
            addReview.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // 초기화 로직이 필요한 경우 여기서 수행할 수 있습니다.
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // 글자 수 업데이트
                    val userInputLength = s?.length ?: 0
                    binding.textCount.text = Editable.Factory.getInstance().newEditable("$userInputLength / 500 자")
                }

                override fun afterTextChanged(s: Editable?) {
                    // 글자가 변경된 후에 필요한 로직을 여기에 작성합니다.
                    mActivity.receiveReview(s.toString())
                }
            })
        }
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddRestaurantReview.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddRestaurantReview().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}