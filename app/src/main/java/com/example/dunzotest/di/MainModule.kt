package com.example.dunzotest.di

import androidx.lifecycle.ViewModel
import com.example.dunzotest.ui.main.PhotoListViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainModule {
    @Binds
    @IntoMap
    @ViewModelKey(PhotoListViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: PhotoListViewModel): ViewModel
}