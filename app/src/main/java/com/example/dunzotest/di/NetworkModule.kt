package com.example.dunzotest.di

import com.example.dunzotest.BuildConfig
import com.example.dunzotest.api.PhotoApi
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
class NetworkModule {
    @Provides
    fun getBaseUrl(): String {
        return BuildConfig.BASE_URL
    }

    @Provides
    fun provideRequestInterceptor(): Interceptor? {
        return Interceptor { chain ->
            var request = chain.request()
            var url = request.url()
            var newUrl = url.newBuilder()
                .addQueryParameter("api_key", BuildConfig.FLICKER_API_KEY)
                .addQueryParameter("method", "flickr.photos.search")
                .addQueryParameter("format", "json")
                .addQueryParameter("per_page", "10")
                .addQueryParameter("nojsoncallback", "1").build()
            var newRequest = request.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(interceptor: Interceptor?): OkHttpClient? {
        val httpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        httpBuilder.addInterceptor(interceptor)
        return httpBuilder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient?, baseUrl: String): Retrofit? {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit?): PhotoApi {
        return retrofit?.create(PhotoApi::class.java)!!
    }
}