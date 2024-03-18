package com.example.assignmentkotlin

import io.realm.RealmObject

open class LocationDataRealm(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var userId: Int = 0 // Add this field
) : RealmObject()
