package com.example.assignmentkotlin

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class User : RealmObject() {
    @PrimaryKey
    var id: Int = 0
    var username: String? = null
    var password: String? = null
    var locations: RealmList<LocationDataRealm>? = null
}
