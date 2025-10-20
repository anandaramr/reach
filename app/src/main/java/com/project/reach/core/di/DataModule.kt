package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.database.ReachDatabase
import com.project.reach.data.respository.NetworkRepository
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.data.respository.IdentityRepository
import com.project.reach.data.respository.MessageRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
import com.project.reach.domain.contracts.IWifiController
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
    fun provideIdentityManager(
        @ApplicationContext context: Context
    ): IdentityManager {
        return IdentityManager(context)
    }

    @Provides
    @Singleton
    fun provideIdentityRepository(
        @ApplicationContext context: Context,
        identityManager: IdentityManager
    ): IIdentityRepository {
        return IdentityRepository(identityManager)
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(
        wifiController: IWifiController,
    ): INetworkRepository {
        return NetworkRepository(wifiController)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        @ApplicationContext context: Context,
    ): IMessageRepository {
        return MessageRepository(ReachDatabase.getDatabase(context).messageDao())
    }
}