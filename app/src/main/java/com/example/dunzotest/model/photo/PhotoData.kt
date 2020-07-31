package com.example.dunzotest.model.photo

import com.example.dunzotest.model.photo.Photo
import com.google.gson.annotations.SerializedName

class PhotoData {
    @SerializedName("photo")
    var photos: ArrayList<Photo>? = null
    var pages:Long = 0
}