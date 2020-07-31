package com.example.dunzotest.di

import android.app.Application
import com.example.dunzotest.ui.main.PhotoListActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class, MainModule::class,CommonModule::class])
interface MainComponent {
    fun inject(activity: PhotoListActivity)

    @Component.Builder
    interface Builder {
        fun build(): MainComponent

        @BindsInstance
        fun application(application: Application): Builder
    }
}