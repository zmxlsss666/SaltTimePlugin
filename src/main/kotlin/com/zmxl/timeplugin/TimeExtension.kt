package com.zmxl.timeplugin

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint
import com.xuncorp.spw.workshop.api.WorkshopApi
import com.zmxl.timeplugin.PlaybackStateHolder
import org.pf4j.Extension
import java.io.File
import java.nio.file.Paths
import java.util.*

@Extension
class TimeExtension : PlaybackExtensionPoint {
    private var hasRestoredPosition = false
    private var isPlayerReady = false
    private var lastMediaPath: String? = null
    private var autoSaveTimer: Timer? = null
    private val AUTO_SAVE_INTERVAL_MS = 150L //默认150ms保存一次

    override fun onStateChanged(state: PlaybackExtensionPoint.State) {
        when (state) {
            PlaybackExtensionPoint.State.Ready -> {
                isPlayerReady = true
                if (!hasRestoredPosition) {
                    restorePosition()
                }
                startAutoSave()
            }
            PlaybackExtensionPoint.State.Idle -> {
                isPlayerReady = false
                lastMediaPath = null
                stopAutoSave()
            }
            PlaybackExtensionPoint.State.Ended -> {
                saveCurrentPosition()
                stopAutoSave()
            }
            else -> {}
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            PlaybackStateHolder.startPositionUpdate()
            startAutoSave()
        } else {
            saveCurrentPosition()
            PlaybackStateHolder.stopPositionUpdate()
            stopAutoSave()
        }
    }

    override fun onPositionUpdated(position: Long) {
        PlaybackStateHolder.setPosition(position)
    }

    override fun onSeekTo(position: Long) {
        PlaybackStateHolder.setPosition(position)
        saveCurrentPosition()
    }

    private fun startAutoSave() {
        stopAutoSave()
        autoSaveTimer = Timer(true)
        autoSaveTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                saveCurrentPosition()
            }
        }, AUTO_SAVE_INTERVAL_MS, AUTO_SAVE_INTERVAL_MS)
    }

    private fun stopAutoSave() {
        autoSaveTimer?.cancel()
        autoSaveTimer = null
    }

    private fun restorePosition() {
        try {
            if (!isPlayerReady || hasRestoredPosition) return
            val savedPosition = loadPosition()
            if (savedPosition > 0) {
                Thread {
                    Thread.sleep(500)
                    try {
                        WorkshopApi.playback.seekTo(savedPosition)
                        hasRestoredPosition = true
                    } catch (e: Exception) {
                    }
                }.start()
            } else {
                hasRestoredPosition = true
            }
        } catch (e: Exception) {
            hasRestoredPosition = true
        }
    }

    private fun saveCurrentPosition() {
        try {
            val position = PlaybackStateHolder.currentPosition
            if (position > 0) {
                savePosition(position)
            }
        } catch (e: Exception) {
        }
    }

    private fun getDataDir(): File {
        val appData = System.getenv("APPDATA")
        val dataPath = Paths.get(appData, "Salt Player for Windows", "workshop", "data", "Salt-time-plugin")
        return dataPath.toFile()
    }

    private fun getTimeFile(): File {
        val dataDir = getDataDir()
        if (!dataDir.exists()) {
            dataDir.mkdirs()
        }
        return File(dataDir, "time.txt")
    }

    private fun savePosition(position: Long) {
        try {
            val timeFile = getTimeFile()
            timeFile.writeText(position.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadPosition(): Long {
        return try {
            val timeFile = getTimeFile()
            if (timeFile.exists()) {
                timeFile.readText().toLong()
            } else {
                0L
            }
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
}