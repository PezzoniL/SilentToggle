package com.st.silenttoggle

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.media.AudioManager


class AudioViewModel: ViewModel() {

    private val _muteStatus = MutableStateFlow(false)
    val muteStatus: StateFlow<Boolean>
        get() = _muteStatus.asStateFlow()

    private val _hasNotificationPolicyAccess = MutableStateFlow(false)
    val hasNotificationPolicyAccess: StateFlow<Boolean> = _hasNotificationPolicyAccess.asStateFlow()

    fun changeMuteStatus(context: Context, muteStatus: Boolean) {
        if (!checkNotificationPolicyAccess(context)) {
            return
        }
        _muteStatus.value = muteStatus
        if (muteStatus) {
            setDeviceSilentMode(context)
        } else {
            setDeviceNormalMode(context)
        }
    }

    fun readInitialMuteStatus(context: Context) {
        checkNotificationPolicyAccess(context)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        _muteStatus.value = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
    }

    fun checkNotificationPolicyAccess(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val hasAccess = notificationManager.isNotificationPolicyAccessGranted
        _hasNotificationPolicyAccess.value = hasAccess
        return hasAccess
    }

    private fun setDeviceSilentMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
        } catch (e: SecurityException) {
            _hasNotificationPolicyAccess.value = false
        }
    }

    private fun setDeviceNormalMode(context: Context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        try {
            audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
        } catch (e: SecurityException) {
            _hasNotificationPolicyAccess.value = false
        }
    }
}