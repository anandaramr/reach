package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.respository.AppRepository
import com.project.reach.network.controllers.WifiController
import com.project.reach.data.respository.IIdentityRepository
import com.project.reach.data.respository.IdentityRepository
import com.project.reach.domain.contracts.IAppRepository
import com.project.reach.domain.contracts.IWifiController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideIdentityRepository(
        @ApplicationContext context: Context
    ): IIdentityRepository {
        return IdentityRepository(context)
    }

    @Provides
    @Singleton
    fun provideWifiController(
        @ApplicationContext context: Context
    ): IWifiController {
        return WifiController(context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        wifiController: IWifiController,
    ): IAppRepository {
        return AppRepository(wifiController)
    }
}