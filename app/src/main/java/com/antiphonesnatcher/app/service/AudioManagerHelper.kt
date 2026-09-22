package com.antiphonesnatcher.app.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent

/**
 * Controller for instant audio muting and media playback suppression during Panic Mode.
 */
class AudioManagerHelper(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var previousMusicVolume: Int = -1
    private var previousRingVolume: Int = -1
    private var previousNotificationVolume: Int = -1
    private var focusRequest: AudioFocusRequest? = null

    /**
     * Instantly halts playing media and mutes active streams.
     */
    fun engageInstantMute() {
        val am = audioManager ?: return

        try {
            // 1. Dispatch media pause/stop key event broadcast to immediately halt media apps (Spotify, YouTube, etc.)
            val pauseIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE))
            }
            context.sendOrderedBroadcast(pauseIntent, null)

            val pauseUpIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE))
            }
            context.sendOrderedBroadcast(pauseUpIntent, null)
        } catch (_: Exception) {}

        try {
            // 2. Request transient audio focus to force external audio players to pause
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val playbackAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener {}
                    .build()

                focusRequest?.let { am.requestAudioFocus(it) }
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            }
        } catch (_: Exception) {}

        try {
            // 3. Save current stream volumes
            previousMusicVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            previousRingVolume = am.getStreamVolume(AudioManager.STREAM_RING)
            previousNotificationVolume = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)

            // 4. Force mute streams immediately
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
                am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0)
                am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_MUSIC, true)
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_RING, true)
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_NOTIFICATION, true)
            }
        } catch (_: Exception) {}
    }

    /**
     * Restores previous audio volume levels when exiting Panic Mode.
     */
    fun restoreAudio() {
        val am = audioManager ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && focusRequest != null) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Exception) {}

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                am.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0)
                am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            } else {
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_MUSIC, false)
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_RING, false)
                @Suppress("DEPRECATION")
                am.setStreamMute(AudioManager.STREAM_NOTIFICATION, false)
            }

            if (previousMusicVolume >= 0) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, previousMusicVolume, 0)
            }
        } catch (_: Exception) {}
    }

    private var alarmPlayer: android.media.MediaPlayer? = null

    /**
     * Blasts high-priority anti-theft siren at maximum volume if permissions are tampered with.
     */
    fun playTamperAlarm() {
        val am = audioManager ?: return
        try {
            val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            am.setStreamVolume(AudioManager.STREAM_ALARM, maxVol, 0)

            if (alarmPlayer == null) {
                val alertUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
                    ?: android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
                alarmPlayer = android.media.MediaPlayer().apply {
                    setDataSource(context, alertUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } else if (alarmPlayer?.isPlaying == false) {
                alarmPlayer?.start()
            }
        } catch (_: Exception) {}
    }

    /**
     * Halts anti-theft alarm siren once authenticated PIN is entered.
     */
    fun stopTamperAlarm() {
        try {
            alarmPlayer?.stop()
            alarmPlayer?.release()
            alarmPlayer = null
        } catch (_: Exception) {}
    }
}
