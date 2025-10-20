package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.domain.contracts.IWifiController
import com.project.reach.network.controllers.DiscoveryController
import com.project.reach.network.controllers.WifiController
import com.project.reach.network.transport.NetworkTransport
import com.project.reach.network.transport.UDPTransport
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWifiController(
        @ApplicationContext context: Context,
        discoveryController: DiscoveryController,
        @UDP udpTransport: NetworkTransport
    ): IWifiController {
        return WifiController(context, discoveryController, udpTransport)
    }

    @Provides
    @Singleton
    @UDP
    fun provideUDPTransport(): NetworkTransport = UDPTransport()

    @Provides
    @Singleton
    fun provideDiscoveryController(
        @ApplicationContext context: Context,
        identityManager: IdentityManager
    ): DiscoveryController {
        return DiscoveryController(context, identityManager)
    }
}