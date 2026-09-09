package com.privacyview.app.service

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.privacyview.app.MainActivity
import com.privacyview.app.PrivacyApp
import com.privacyview.app.R

@RequiresApi(Build.VERSION_CODES.N)
class PrivacyTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pending = android.app.PendingIntent.getActivity(
                    this, 0, intent, android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
                startActivityAndCollapse(pending)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }

        val prefs = PrivacyApp.instance.preferences
        val nextState = !prefs.isPrivacyEnabledSync()
        prefs.setPrivacyEnabled(nextState)

        if (nextState) {
            PrivacyOverlayService.start(this)
        } else {
            PrivacyOverlayService.stop(this)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = PrivacyApp.instance.preferences
        val isEnabled = prefs.isPrivacyEnabledSync()

        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.tile_label)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_privacy_tile)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isEnabled) {
                "${prefs.getPrivacyModeSync().displayName} • ${(prefs.getStrengthSync() * 100).toInt()}%"
            } else {
                getString(R.string.privacy_off)
            }
        }

        tile.updateTile()
    }
}
