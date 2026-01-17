package com.st.silenttoggle

import android.app.PendingIntent
import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class SilentToggleService : TileService() {

    private val viewModel = AudioViewModel()

    // Called when the user adds the tile to Quick Settings
    override fun onTileAdded() {
        super.onTileAdded()
    }

    // Called when the tile becomes visible
    override fun onStartListening() {
        super.onStartListening()
        viewModel.readInitialMuteStatus(this)
        updateTile()
    }

    // Handle tile click
    override fun onClick() {
        super.onClick()
        
        if (!viewModel.checkNotificationPolicyAccess(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
            return
        }

        val newMuteStatus = !viewModel.muteStatus.value
        viewModel.changeMuteStatus(this, newMuteStatus)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val isMuted = viewModel.muteStatus.value
        tile.state = if (isMuted) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}