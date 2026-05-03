package com.example.mobile.data.api

import com.example.mobile.utils.Constants
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {

    private fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private fun createOkHttpClient(tokenProvider: (() -> String?)? = null): OkHttpClient {
        val builder = OkHttpClient.Builder()

        tokenProvider?.let { provider ->
            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val authorized = original.newBuilder()
                    .addHeader(
                        Constants.AUTHORIZATION_HEADER,
                        "${Constants.BEARER_PREFIX}${provider()}"
                    )
                    .build()
                chain.proceed(authorized)
            }
            builder.addInterceptor(authInterceptor)
        }

        return builder.build()
    }

    fun createApiService(tokenProvider: (() -> String?)? = null): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(createOkHttpClient(tokenProvider))
            .addConverterFactory(MoshiConverterFactory.create(createMoshi()))
            .build()

        return retrofit.create(ApiService::class.java)
    }
}