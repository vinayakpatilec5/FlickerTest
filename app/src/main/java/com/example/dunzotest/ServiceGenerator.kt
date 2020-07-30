package com.example.dunzotest

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

object ServiceGenerator {
    const val BASE_URL = "https://api.flickr.com/"
    lateinit var retrofit : Retrofit
    lateinit var context: Context
    fun initClient(context: Context){
        ServiceGenerator.context = context
        retrofit = Retrofit.Builder().baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(getClient())
            .build()
    }

    fun provideRetrofit(): Retrofit? {
        return retrofit;
    }

    private fun getClient(): OkHttpClient? {
        val httpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
        httpBuilder.addInterceptor(provideRequestInterceptor())
        return httpBuilder.build()
    }

    private fun provideRequestInterceptor(): Interceptor? {
        return Interceptor { chain ->
            var request = chain.request()
            var url = request.url()
            var newUrl = url.newBuilder()
                .addQueryParameter("api_key","062a6c0c49e4de1d78497d13a7dbb360")
                .addQueryParameter("method","flickr.photos.search")
                .addQueryParameter("format","json")
                .addQueryParameter("per_page","10")
                .addQueryParameter("nojsoncallback","1").build()
            var newRequest = request.newBuilder().url(newUrl).build()
            chain.proceed(newRequest)
        }
    }

}