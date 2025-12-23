package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.local.IdentityManager
import com.project.reach.data.local.database.ReachDatabase
import com.project.reach.data.respository.ContactRepository
import com.project.reach.data.respository.FileRepository
import com.project.reach.data.respository.IdentityRepository
import com.project.reach.data.respository.MessageRepository
import com.project.reach.data.respository.NetworkRepository
import com.project.reach.domain.contracts.IContactRepository
import com.project.reach.domain.contracts.IFileRepository
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.domain.contracts.IMessageRepository
import com.project.reach.domain.contracts.INetworkController
import com.project.reach.domain.contracts.INetworkRepository
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
        networkController: INetworkController,
    ): INetworkRepository {
        return NetworkRepository(networkController)
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext context: Context
    ): IFileRepository {
        return FileRepository(context)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        @ApplicationContext context: Context,
        contactRepository: IContactRepository,
        networkController: INetworkController,
        fileRepository: IFileRepository,
        identityManager: IdentityManager
    ): IMessageRepository {
        val database = ReachDatabase.getDatabase(context)
        return MessageRepository(
            messageDao = database.messageDao(),
            mediaDao = database.mediaDao(),
            contactRepository = contactRepository,
            networkController = networkController,
            identityManager = identityManager,
            fileRepository = fileRepository
        )
    }

    @Provides
    @Singleton
    fun provideContactRepository(
        @ApplicationContext context: Context
    ): IContactRepository {
        val database = ReachDatabase.getDatabase(context)
        return ContactRepository(contactDao = database.contactDao())
    }
}