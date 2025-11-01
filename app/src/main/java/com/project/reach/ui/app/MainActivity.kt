package com.project.reach.ui.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.service.ForegroundServiceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity: ComponentActivity() {

    @Inject
    lateinit var identityRepository: IIdentityRepository

    private val coreProvider = CoreProvider(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isOnboardingRequired = identityRepository.isOnboardingRequired()

        enableEdgeToEdge()
        setContent {
            coreProvider {
                ReachApp(
                    isOnboardingRequired = isOnboardingRequired,
                    startService = ::startReachService
                )
            }
        }
    }

    private fun startReachService() {
        coreProvider.permissionHandler.onNotificationPermissionGranted(
            onGranted = { ForegroundServiceManager.startService(applicationContext) }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
