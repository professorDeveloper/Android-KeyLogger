package com.ratx.hidden_rat.model

data class NotificationModel(
    val packageName: String,
    val title: String,
    val text: String,
    val timeStamp: String,
    val dayTime: String,
){
    constructor() : this("", "", "", "", "")

}
