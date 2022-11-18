package com.wellnest.one.di

import com.wellnest.one.data.remote.*
import com.wellnest.one.network.api.IAccountApi
import com.wellnest.one.network.api.IDeviceApi
import com.wellnest.one.network.api.IProfileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by Hussain on 07/11/22.
 */
@Module
@InstallIn(SingletonComponent::class)
object ApiProvider {

    @Provides
    @Singleton
    fun provideAccountInterface(retrofit : Retrofit) : IAccountApi {
        return retrofit.create(IAccountApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAccountsApi(iAccountApi : IAccountApi) : AccountRepository {
        return AccountRepositoryImpl(iAccountApi)
    }

    @Provides
    @Singleton
    fun provideProfileInterface(retrofit: Retrofit) : IProfileApi {
        return retrofit.create(IProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApi(iProfileApi: IProfileApi) : ProfileRepository {
        return ProfileRepositoryImpl(iProfileApi)
    }

    @Provides
    @Singleton
    fun provideDeviceInterface(retrofit: Retrofit) : IDeviceApi {
        return retrofit.create(IDeviceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDeviceApi(iDeviceApi: IDeviceApi) : DeviceRepository {
        return DeviceRepositoryImpl(iDeviceApi)
    }
}