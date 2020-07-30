package com.example.dunzotest.model

import com.example.dunzotest.model.PhotoData
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photos")
    var photoData: PhotoData? = null
}