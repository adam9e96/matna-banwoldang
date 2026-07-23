package com.example.restaurantreview.main.firebase

import android.app.Application
import com.elvishew.xlog.LogConfiguration
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.AndroidPrinter
import com.example.restaurantreview.main.settings.LoggerConfig
import com.google.firebase.database.FirebaseDatabase

// 앱이 시작될 때 Firebase Realtime Database의 persistence를 설정
/**
 * Firebase Realtime Database에서 데이터를 오프라인에서도 사용할 수 있도록 하는 기능을 활성화/비활성화 여부를 설정
 *
 * true : 오프라인에서도 데이터가 지속적으로 유지되도록 함.
 * false : 오프라인 데이터 유지 기능을 비활성화
 *
 * 사용
 * 파이어베이스의 Realtime Database를 사용 할 때, 데이터의 오프라인 기능을 설정하려면
 * FirebaseDatabase 인스턴스를 초기화하기 전에 SetPersistenceEnabled(true)를 호출해야 한다.
 *
 * 보통 애플리케이션의 초기화 단계(onCreate)에서 설정 <-- FirebaseDatabase 인스턴스가 초기화 되기전에 먼저 호출되어야 예외가 발생하지 않는다.
 * (아래와 같은 설정을 하지않았을 때 엑티비티 전환하면서 예외가 발생 했었음)
 *
 * 해당 클래스를 매니페스트에도 설정해야함
 * https://stackoverflow.com/questions/37753991/com-google-firebase-database-databaseexception-calls-to-setpersistenceenabled
 */
class FirebaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase Realtime Database의 오프라인 지속성을 활성화
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)


        // XLog 초기화
        val config = LogConfiguration.Builder()
            .tag("반월당 Log")
            .enableThreadInfo()
            .enableStackTrace(2)
            .enableBorder()
            .logLevel(LogLevel.ALL)  // 로그 레벨 설정
            .build()

        val androidPrinter = AndroidPrinter()
        XLog.init(config, androidPrinter)

        // 기타 초기화 작업
        LoggerConfig.initialize()

        // 로그 기록 사용방법
//        XLog.d("디버그 로그 메시지")
//        XLog.i("정보 로그 메시지")
//        XLog.w("경고 로그 메시지")
//        XLog.e("에러 로그 메시지")


    }
}