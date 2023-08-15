package com.sample.locations

import io.realm.RealmObject

open class MyLocation(
    var timestamp: String = "0",
    var latitude: String = "0.0",
    var longitude: String = "0.0",
    var altitude: String = "0.0",
    var accuracy: String = "0.0",
    var speed: String = "0.0",
) : RealmObject()