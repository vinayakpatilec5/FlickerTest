package com.example.dunzotest.di

import com.example.dunzotest.ui.main.domain.PhotoListRepository
import com.example.dunzotest.ui.main.domain.PhotoListRepositoryImpl
import dagger.Binds
import dagger.Module


@Module
abstract class CommonModule {
    @Binds
    abstract fun bindApiService(userListRepository: PhotoListRepositoryImpl): PhotoListRepository

}