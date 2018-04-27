package com.shopapp.magento.retrofit

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.shopapp.magento.api.response.AttributeValue
import com.shopapp.magento.deserializer.AttributeValueDeserializer
import com.shopapp.magento.deserializer.DateDeserializer
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object RestClient {

    private const val TIMEOUT: Long = 10
    private const val cacheSize: Long = 10 * 1024 * 1024 // 10 MB

    fun providesRetrofit(context: Context, baseUrl: String, apiKey: String): Retrofit {

        val gson = GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .registerTypeAdapter(AttributeValue::class.java, AttributeValueDeserializer())
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .create()

        val cache = Cache(context.cacheDir, cacheSize)

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(providesOkHttp(apiKey, cache))
            .build()
    }

    private fun providesOkHttp(apiKey: String, cache: Cache): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(cache)
            .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(getLoggingInterceptor())
            .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(getAuthInterceptor(apiKey))
            .readTimeout(TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun getAuthInterceptor(apiKey: String): Interceptor {
        return Interceptor { chain ->
            val builder = chain.request().newBuilder()
            builder.addHeader("Authorization", "Bearer $apiKey")
            return@Interceptor chain.proceed(builder.build())
        }
    }

    private fun getLoggingInterceptor(): Interceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }
}