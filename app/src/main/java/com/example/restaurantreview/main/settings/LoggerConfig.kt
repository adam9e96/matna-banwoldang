package com.example.restaurantreview.main.settings

import com.google.common.flogger.FluentLogger


object LoggerConfig {
    private val logger: FluentLogger = FluentLogger.forEnclosingClass()

    fun initialize() {
        // Logger 초기화 로직
        // 필요에 따라 다른 설정을 추가합니다.
        logger.atInfo().log("Logger 초기화됨")
    }
}
