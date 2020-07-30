package com.example.dunzotest.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.dunzotest.R
import com.example.dunzotest.model.Photo
import com.example.dunzotest.api.PhotoApi
import com.example.dunzotest.model.PhotoResponse
import com.example.dunzotest.util.MainUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainViewModel @Inject constructor(application: Application,var repo: PhotoApi) : AndroidViewModel(application) {
    //    var repo: Api
    var pageNo = 0
    var photoList : MutableLiveData<ArrayList<Photo>> = MutableLiveData<ArrayList<Photo>>()

    var allPhotoList = ArrayList<Photo>()

    var loading: MutableLiveData<Boolean> = MutableLiveData()
    var paginationLoading: MutableLiveData<Boolean> = MutableLiveData()
    var paginationDone: Boolean = false
    var error: MutableLiveData<String> = MutableLiveData()
    var paginationError: MutableLiveData<String> = MutableLiveData()
    var searchText:String=""
    var disp: CompositeDisposable = CompositeDisposable()


    val searchEditText = PublishSubject.create<String>()

    fun checkForEmptyString(searchText:String){
        if(searchText.trim().isEmpty()){
            error.value = MainUtil.getMessage(getApplication(), R.string.search_msg)
            loading.value = false
            paginationLoading.value=false
            return
        }
        loadInitData(searchText)
    }

    fun loadInitData(searchText:String) {
        if(!this.searchText.equals(searchText)) {
            pageNo = 0
            allPhotoList.clear()
            loading.value = true
            this.searchText = searchText
            paginationDone = false
            loadPageData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        disp.clear()
    }

    fun loadNextPage(){
        paginationLoading.value = true
        loadPageData()
    }

    fun loadPageData(){
        disp.add(repo.getPhotos(searchText,pageNo)
            .subscribeOn(Schedulers.io())
            .map (object: Function<PhotoResponse,ArrayList<Photo>> {
                override fun apply(response: PhotoResponse): ArrayList<Photo> {
                    var list = ArrayList<Photo>()
                    if(response.photoData?.photos?.size?:0 > 0) {
                        for(photo in response?.photoData?.photos!!){
                            photo.imageUrl = "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_m.jpg"
                            list.add(photo)
                        }
                    }
                    return list
                }
            })
            .observeOn(AndroidSchedulers.mainThread()).subscribe ({
                loading.value = false
                paginationLoading.value = false
                if(it.size > 0) {
                    allPhotoList.addAll(it)
                    pageNo++;
                    photoList.value = it
                }else{
                    paginationDone = true
                }

            },{
                paginationLoading.value = false
                loading.value = false
                if(pageNo==0) {
                    error.value = MainUtil.getErrorTye(it, getApplication())
                }else{
                    paginationError.value = MainUtil.getErrorTye(it, getApplication())
                }
            }))

    }



    fun setSearchTextListener(){
        if(!searchEditText.hasObservers()) {
            disp.add(searchEditText.debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    checkForEmptyString(it)
                })
            )
        }
    }

}