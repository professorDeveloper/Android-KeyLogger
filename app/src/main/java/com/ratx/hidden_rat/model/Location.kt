package com.ratx.hidden_rat.model
/**
 * Created by Azamov on 5/06/25.
 */
class Location {

    var latitude: Double? = null
    var longitude: Double? = null
    var address: String? = null
    var dateTime: String? = null

    constructor() {}

    constructor(latitude: Double, longitude: Double, address: String, dateTime: String) {
        this.latitude = latitude
        this.longitude = longitude
        this.address = address
        this.dateTime = dateTime
    }
}