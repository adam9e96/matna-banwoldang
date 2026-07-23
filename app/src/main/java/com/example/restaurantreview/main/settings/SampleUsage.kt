package com.example.restaurantreview.main.settings

// 사용자 정의 애노테이션

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class SampleUsage(val example: String)