package com.example.restaurantreview.main.view

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.example.restaurantreview.databinding.FragmentAddRestaurantDataBinding
import com.example.restaurantreview.main.dialog.AddRePermissionDialog
import com.example.restaurantreview.main.dialog.AddRePermissionDialogInterface
import com.example.restaurantreview.main.dialog.ConfirmDialogConfirmDialogTime
import com.example.restaurantreview.main.dialog.ConfirmDialogTimeInterface
import com.example.restaurantreview.main.helper.AddCategoryHelper
import com.example.restaurantreview.main.helper.AddTagHelper
import com.example.restaurantreview.main.helper.DayHelper
import java.io.FileOutputStream
import java.util.Calendar
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddRestaurantDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddRestaurantDataFragment : Fragment(), ConfirmDialogTimeInterface,
    AddRePermissionDialogInterface {
    lateinit var binding: FragmentAddRestaurantDataBinding
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val PERMISSION_Album = 101 // 앨범 권한 처리
    var dataString: String = ""
    val CAMERA_CODE = 102
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    } // onCreate() END

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddRestaurantDataBinding.inflate(inflater, container, false)

        binding.run {
            val mActivity = activity as AddRestaurantDataActivity
            restaurantName.addTextChangedListener {
                mActivity.receiveName(restaurantName.text.toString())
            }
            openTimeStart.setOnClickListener { getTime(openTimeStart, openTimeStart.context) }
            openTimeEnd.setOnClickListener { getTime(openTimeEnd, openTimeEnd.context) }
            breakTimeStart.setOnClickListener { getTime(breakTimeStart, breakTimeStart.context) }
            breakTimeEnd.setOnClickListener {
                getTime(breakTimeEnd, breakTimeEnd.context)

            }
            addImage.setOnClickListener {
                if(context?.let { it1 -> ContextCompat.checkSelfPermission(it1, android.Manifest.permission.READ_MEDIA_IMAGES) } == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    dialog()
                }else{
                    requestPermission()
                }

                // 위에서 접근시 이전 값이 자꾸 적용되어서 부득이하게 다른 버튼 누를떄 값이 넘어가게 번겨변
                mActivity.receiveBusinessHourStart(openTimeStart.text.toString())
                mActivity.receiveBusinessHourEnd(openTimeEnd.text.toString())
                mActivity.receiveBreakTimeStart(breakTimeStart.text.toString())
                mActivity.receiveBreakTimeEnd(breakTimeEnd.text.toString())
            }
        }
        // 태그 관련
        AddCategoryHelper.setupCategory(this, binding)
        AddTagHelper.setupCategory(this, binding)
        DayHelper.setupCategory(this, binding)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddRestaurantDataFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddRestaurantDataFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun getTime(button: Button, context: Context) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->

            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

//            button.text = SimpleDateFormat("HH:mm").format(cal.time)
            button.text = when {
                hour < 10 -> {
                    if (minute < 10) {
                        "0${hour}:0${minute}"
                    } else {
                        "0${hour}:${minute}"
                    }

                }

                else -> {
                    if (minute < 10) {
                        "${hour}:0${minute}"
                    } else {
                        "${hour}:${minute}"
                    }
                }
            }
        }

        TimePickerDialog(
            context,
            timeSetListener,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()


    }


    //  앨범 에서 사진 가져오기위한  사전작업 시작
    @SuppressLint("IntentReset")
    fun openAlbum(view: View) {    // 사진등록 버튼을 누르면 실행됨 이미지 고를 갤러리 오픈
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "load image"), PERMISSION_Album)


    }

    @Deprecated("Deprecated in Java")
    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PERMISSION_Album) {
            val dataUri = data?.data
            dataString = dataUri.toString()
            Toast.makeText(context, "$dataUri", Toast.LENGTH_SHORT).show()
            activity as AddRestaurantDataActivity
            (activity as AddRestaurantDataActivity).receiveUrl(dataString)
            try {
                context?.let {
                    Glide.with(it).load(dataUri).override(100, 100).into(binding.miniImage)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "${e.message} 실패하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == CAMERA_CODE) {
            if (data?.extras?.get("data") != null) {
                val img = data.extras?.get("data") as Bitmap
                val uri = saveFile(Date().toString(), "image/jpeg", img)
                activity as AddRestaurantDataActivity
                (activity as AddRestaurantDataActivity).receiveUrl(uri.toString())
                binding.miniImage.setImageURI(uri)
            }
        }
    }

    private fun dialog() {
        // 다이얼로그
        val title = "카메라 앨범 중 하나를 선택해주세요."

        val dialog = ConfirmDialogConfirmDialogTime(this@AddRestaurantDataFragment, title, null, "카메라", 0)
        // 알림창이 띄워져있는 동안 배경 클릭 막기
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "ConfirmDialog") }
    }

    override fun onClickYesButton(id: Int) {
        callCamera()
    }

    override fun onClickNoButton(id: Int) {
        Log.i("noting", id.toString())
        openAlbum(binding.miniImage)
    }

    // 카메라 촬영 - 권한 처리
    private fun callCamera() {
        val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(itt, CAMERA_CODE)


    }


    @SuppressLint("Recycle")
    fun saveFile(fileName: String, mimeType: String, bitmap: Bitmap): Uri? {

        val CV = ContentValues()

        // MediaStore 에 파일명, mimeType 을 지정
        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        // 안정성 검사
        CV.put(MediaStore.Images.Media.IS_PENDING, 1)

        // MediaStore 에 파일을 저장
        // requireContext().contentResolver 이게 뭘까??
        val uri = requireContext().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            CV
        )
        Log.i("uri", uri.toString())

        if (uri != null) {
            val scriptor = requireContext().contentResolver.openFileDescriptor(uri, "w")

            val fos = FileOutputStream(scriptor?.fileDescriptor)

            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, fos)
            fos.close()

            CV.clear()
            // IS_PENDING 을 초기화
            CV.put(MediaStore.Images.Media.IS_PENDING, 0)
            requireContext().contentResolver.update(uri, CV, null, null)
        }
        return uri
    }

    // 메인에있는 함수를 한번에 호출하려 할시 오류발생 중간 유통자를 만듬


//    @SuppressLint("SimpleDateFormat")
//    private fun dayofTime(dayAndTime: String) {
//        dayof = SimpleDateFormat("yyyy-MM-dd").format(com.google.firebase.Timestamp)
//
//    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(){
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_MEDIA_IMAGES), 1000)
        }else{
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
        }

    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "super.onRequestPermissionsResult(requestCode, permissions, grantResults)",
        "androidx.fragment.app.Fragment"
    )
    )
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
            dialog()
        }else{
            addRequestPermissionDialog()
        }
    }

    private fun addRequestPermissionDialog(){
        val dialog = AddRePermissionDialog(this@AddRestaurantDataFragment,0)
        dialog.isCancelable = false
        activity?.let { dialog.show(it.supportFragmentManager, "permission") }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun permissionYesButton(id: Int) {
        if(shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
            requestPermission()
        }
    }

    override fun permissionNoButton(id: Int) {
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}