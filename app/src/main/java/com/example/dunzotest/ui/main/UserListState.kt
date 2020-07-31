package com.example.dunzotest.ui.main

import com.example.dunzotest.model.photo.Photo

sealed class UserListState() {
    data class LoadingState(var loading:Boolean):UserListState()
    data class PaginationLoadingState(var loading:Boolean):UserListState()
    data class ErrorState(var error: String):UserListState()
    data class PaginationErrorState(var error: String):UserListState()
    data class SuccessListState(val photoList: ArrayList<Photo>):UserListState()

}
