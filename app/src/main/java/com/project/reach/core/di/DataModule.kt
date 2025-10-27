package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.database.ReachDatabase
import com.project.reach.data.respository.IdentityRepository
import com.project.reach.data.respository.MessageRepository
import com.project.reach.data.respository.NetworkRepository
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkRepository
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
    fun provideIdentityManager(
        @ApplicationContext context: Context
    ): IdentityManager {
        return IdentityManager(context)
    }

    @Provides
    @Singleton
    fun provideIdentityRepository(
        identityManager: IdentityManager
    ): IIdentityRepository {
        return IdentityRepository(identityManager)
    }

    @Provides
    @Singleton
    fun provideNetworkRepository(
        wifiController: IWifiController,
        @UDP udpTransport: NetworkTransport,
        @TCP tcpTransport: NetworkTransport,
    ): INetworkRepository {
        return NetworkRepository(wifiController, udpTransport, tcpTransport)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        @ApplicationContext context: Context,
        wifiController: IWifiController,
        identityManager: IdentityManager
    ): IMessageRepository {
        val database = ReachDatabase.getDatabase(context)
        return MessageRepository(
            messageDao = database.messageDao(),
            contactDao = database.contactDao(),
            wifiController = wifiController,
            identityManager = identityManager
        )
    }
}