package com.example.dunzotest.di

import androidx.lifecycle.ViewModel
import com.example.dunzotest.ui.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindHomeWidgetViewModel(homeWidgetViewModel: MainViewModel): ViewModel
}