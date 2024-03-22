package com.azamovhudstc.androidkeylogger.model

import java.io.Serializable

data class SMSData(
    val message: String,
    val sender: String,
    val sentTime: String,
): Serializable