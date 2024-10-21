package com.ratx.hidden_rat.model

import java.io.Serializable

data class SMSData(
    val message: String,
    val sender: String,
    val sentTime: String,
): Serializable