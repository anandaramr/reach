package com.project.reach.core.di

import android.content.Context
import com.project.reach.data.respository.IIdentityRepository
import com.project.reach.data.respository.IdentityRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IdentityModule {

    @Provides
    @Singleton
    fun provideIdentityRepository(
        @ApplicationContext context: Context
    ): IIdentityRepository {
        return IdentityRepository(context)
    }
}