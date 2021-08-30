package com.mall.login.di

import com.mall.login.api.ApiLoginService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class LoginNewWorkModule {
    @Singleton
    @Provides
    fun providerLoginService(retrofit: Retrofit): ApiLoginService {
        return retrofit.create(ApiLoginService::class.java)
    }
}