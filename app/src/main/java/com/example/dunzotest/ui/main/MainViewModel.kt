package com.example.dunzotest.ui.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.dunzotest.R
import com.example.dunzotest.model.Photo
import com.example.dunzotest.api.PhotoApi
import com.example.dunzotest.model.PhotoResponse
import com.example.dunzotest.util.MainUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.Exception
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

    fun resetList(){
        pageNo = 0
        allPhotoList.clear()
        loading.postValue(true)
        paginationDone = false
    }

    fun setEmptySearchTextData(){
        if(searchText.trim().isEmpty()) {
            error.postValue(MainUtil.getMessage(getApplication(), R.string.search_msg))
            loading.postValue(false)
            paginationLoading.postValue(false)
        }
    }


    override fun onCleared() {
        super.onCleared()
        disp.clear()
    }

    fun loadNextPage(){
        paginationLoading.postValue(true)
        loadPageData()
    }

    fun loadPageData(){
        disp.add(repo.getPhotos(searchText,pageNo)
            .subscribeOn(Schedulers.io())
            .map (object: Function<PhotoResponse,ArrayList<Photo>> {
                override fun apply(response: PhotoResponse): ArrayList<Photo> {
                    return getListWithImage(response)
                }
            })
            .observeOn(AndroidSchedulers.mainThread()).subscribe ({
                handleSuccessResult(it)
            },{
                handleFailedData(it)
            }))

    }

    fun getListWithImage(response: PhotoResponse?):ArrayList<Photo>{
        var list = ArrayList<Photo>()
        try {
            if (response?.photoData?.photos?.size ?: 0 > 0) {
                for (photo in response?.photoData?.photos!!) {
                    photo.imageUrl =
                        "https://farm${photo.farm}.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}_m.jpg"
                    list.add(photo)
                }
            }
        }catch (e:Exception){}
        return list
    }

    fun handleSuccessResult(list:ArrayList<Photo>){
        loading.postValue(false)
        paginationLoading.postValue(false)
        error.postValue("")
        if(list.size > 0) {
            allPhotoList.addAll(list)
            pageNo++;
            photoList.postValue(list)
        }else{
            if(searchText.trim().isEmpty()) {
                setEmptySearchTextData()
            }else if(pageNo==0){
                error.postValue(MainUtil.getMessage(getApplication(),R.string.no_result_found))
            }else {
                paginationDone = true
            }
        }
    }

    fun handleFailedData(t:Throwable?){
        Log.e("===========",""+t.toString())
            t?.let {
                paginationLoading.postValue(false)
                loading.postValue(false)
                if (pageNo == 0) {
                    error.postValue(MainUtil.getErrorTye(t, getApplication()))
                } else {
                    paginationError.postValue(MainUtil.getErrorTye(t, getApplication()))
                }
            }
    }

    fun setSearchTextListener(){
        if(!searchEditText.hasObservers()) {
            disp.add(searchEditText.debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .filter(object :Predicate<String>{
                    override fun test(t: String): Boolean {
                        searchText=t
                        if(t.trim().isEmpty()){
                            setEmptySearchTextData()
                            return false
                        }
                        return true
                    }
                })
                .switchMapSingle (object: Function<String,Single<PhotoResponse>?> {
                    override fun apply(string: String): Single<PhotoResponse>? {
                        searchText = string
                        resetList()
                        return repo.getPhotos(searchText,pageNo).subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                    }
                })
                .map (object: Function<PhotoResponse?,ArrayList<Photo>> {
                    override fun apply(response: PhotoResponse): ArrayList<Photo>? {
                        return getListWithImage(response)
                    }
                })
                .subscribe({
                    handleSuccessResult(it)
                },{
                    handleFailedData(it)
                }))
        }
    }
}