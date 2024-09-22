package com.azamovhudstc.androidkeylogger.model


data class CallRecord(
    val phoneNumber: String,
    val startTimeMillis: String,
    val endTimeMillis: String,
    val durationMillis: String,
    val fileLink: String
)