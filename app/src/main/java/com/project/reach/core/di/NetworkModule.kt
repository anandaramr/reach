package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.controllers.WifiController
import com.project.reach.network.discovery.NsdDiscoveryController
import com.project.reach.network.transport.NetworkTransport
import com.project.reach.network.transport.TCPTransport
import com.project.reach.network.transport.UDPTransport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWifiController(
        @ApplicationContext context: Context,
        @UDP udpTransport: NetworkTransport,
        @TCP tcpTransport: NetworkTransport,
        identityManager: IdentityManager,
    ): IWifiController {
        return WifiController(context, udpTransport, tcpTransport, identityManager)
    }

    @Provides
    @Singleton
    @UDP
    fun provideUDPTransport(): NetworkTransport = UDPTransport()

    @Provides
    @Singleton
    @TCP
    fun provideTCPTransport(): NetworkTransport = TCPTransport()

    @Provides
    @Singleton
    fun provideDiscoveryController(
        @ApplicationContext context: Context,
        identityManager: IdentityManager
    ): NsdDiscoveryController {
        return NsdDiscoveryController(context, identityManager)
    }
}