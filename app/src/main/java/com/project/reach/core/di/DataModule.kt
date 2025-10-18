package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.respository.AppRepository
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.data.respository.IdentityRepository
import com.project.reach.domain.contracts.IAppRepository
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.transport.NetworkTransport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideIdentityRepository(
        @ApplicationContext context: Context
    ): IIdentityRepository {
        return IdentityRepository(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        wifiController: IWifiController,
    ): IAppRepository {
        return AppRepository(wifiController)
    }
}