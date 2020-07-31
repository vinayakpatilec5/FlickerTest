package com.example.dunzotest.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.dunzotest.R
import com.example.dunzotest.model.photo.Photo
import com.example.dunzotest.ui.main.domain.PhotoListRepository
import com.example.dunzotest.util.MainUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PhotoListViewModel @Inject constructor(
    application: Application,
    var photoListRepository: PhotoListRepository) :
    AndroidViewModel(application) {
    //    var repo: Api
    var pageNo = 0
    var allPhotoList = ArrayList<Photo>()
    var paginationDone: Boolean = false


    var stateLiveData: MutableLiveData<UserListState> = MutableLiveData<UserListState>()

    var searchText: String = ""
    var disp: CompositeDisposable = CompositeDisposable()
    val searchEditText = PublishSubject.create<String>()

    fun resetList() {
        pageNo = 0
        allPhotoList.clear()
        paginationDone = false
        setLoadingState()
    }

    fun setEmptySearchTextData() {
        if (searchText.trim().isEmpty()) {
            setErrorState(MainUtil.getMessage(getApplication(), R.string.search_msg))
        }
    }


    override fun onCleared() {
        super.onCleared()
        disp.clear()
    }

    fun loadNextPage() {
        setPaginationLoadingState()
        disp.add(
            photoListRepository.getPhotos(searchText, pageNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    handleSuccessResult(it)
                }, {
                    handleFailedData(it)
                })
        )

    }


    fun handleSuccessResult(list: ArrayList<Photo>) {
        if (list.size > 0) {
            allPhotoList.addAll(list)
            setSuccessData(list)
            pageNo++
        } else {
            if (searchText.trim().isEmpty()) {
                setErrorState(MainUtil.getMessage(getApplication(), R.string.search_msg))
            } else if (pageNo == 0) {
                setErrorState(MainUtil.getMessage(getApplication(), R.string.no_result_found))
            } else {
                paginationDone = true
            }
        }
    }


    fun handleFailedData(t: Throwable?) {
        t?.let {
            if (pageNo == 0) {
                setErrorState(MainUtil.getErrorTye(t, getApplication()))
            } else {
                setPaginationErrorState(MainUtil.getErrorTye(t, getApplication()))
            }
        }
    }

    fun setSearchTextListener() {
        if (!searchEditText.hasObservers()) {
            disp.add(
                searchEditText.debounce(300, TimeUnit.MILLISECONDS)
                    .distinctUntilChanged()
                    .filter(object : Predicate<String> {
                        override fun test(t: String): Boolean {
                            searchText = t
                            if (t.trim().isEmpty()) {
                                setEmptySearchTextData()
                                return false
                            }
                            return true
                        }
                    })
                    .switchMapSingle(object : Function<String, Single<ArrayList<Photo>>> {
                        override fun apply(string: String): Single<ArrayList<Photo>> {
                            searchText = string
                            resetList()
                            return photoListRepository.getPhotos(searchText, pageNo)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                        }
                    })
                    .subscribe({
                        handleSuccessResult(it)
                    }, {
                        handleFailedData(it)
                    })
            )
        }
    }

    //=====================================================================================================
    //handle all states
    fun setSuccessData(list: ArrayList<Photo>) {
        stateLiveData.postValue(UserListState.SuccessListState(list))
    }

    fun setErrorState(msg: String) {
        stateLiveData.postValue(UserListState.ErrorState(msg))
    }

    fun setLoadingState() {
        stateLiveData.postValue(UserListState.LoadingState(true))
    }

    fun setPaginationLoadingState() {
        stateLiveData.postValue(UserListState.PaginationLoadingState(true))
    }

    fun setPaginationErrorState(msg: String) {
        stateLiveData.postValue(UserListState.PaginationErrorState(msg))
    }


}