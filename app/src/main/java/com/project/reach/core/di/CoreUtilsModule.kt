package com.project.reach.core.di

import android.app.Activity
import androidx.activity.ComponentActivity
import com.project.reach.permission.PermissionHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object CoreUtilsModule {

    @Provides
    @ActivityScoped
    fun providePermissionHandler(activity: Activity): PermissionHandler {
        return PermissionHandler(activity as ComponentActivity)
    }
}