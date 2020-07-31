package com.example.dunzotest.ui.main.domain

import com.example.dunzotest.api.PhotoApi
import com.example.dunzotest.model.photo.Photo
import com.example.dunzotest.model.photo.PhotoResponse
import io.reactivex.Single
import io.reactivex.functions.Function
import javax.inject.Inject


interface PhotoListRepository {
    fun getPhotos(searchText: String, pageNo: Int): Single<ArrayList<Photo>>
}

class PhotoListRepositoryImpl @Inject constructor(var photoApi: PhotoApi) : PhotoListRepository {
    override fun getPhotos(searchText: String, pageNo: Int): Single<ArrayList<Photo>> {
        return photoApi.getPhotos(searchText, pageNo).map(object :
            Function<PhotoResponse, ArrayList<Photo>> {
            override fun apply(response: PhotoResponse): ArrayList<Photo>? {
                return photoListModelMapper(response)
            }
        })
    }

    fun photoListModelMapper(response: PhotoResponse?): ArrayList<Photo> {
        var list = ArrayList<Photo>()
        if (response?.photoData?.photos?.size ?: 0 > 0) {
            for (photo in response?.photoData?.photos!!) {
                photo.imageUrl =
                    "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_m.jpg"
                list.add(photo)
            }
        }
        return list
    }

}