package com.zmxl.timeplugin

import com.xuncorp.spw.workshop.api.PlaybackExtensionPoint
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object PlaybackStateHolder {
    @Volatile
    var currentMedia: PlaybackExtensionPoint.MediaItem? = null
    @Volatile
    var isPlaying: Boolean = false
    @Volatile
    var currentState: PlaybackExtensionPoint.State = PlaybackExtensionPoint.State.Idle
    

    @Volatile
    var currentPosition: Long = 0L
    private var positionUpdateTimer: Timer? = null
    private var lastPositionUpdateTime: Long = System.currentTimeMillis()
    

    fun startPositionUpdate() {
        stopPositionUpdate()
        
        positionUpdateTimer = Timer(true)
        lastPositionUpdateTime = System.currentTimeMillis()
        
        positionUpdateTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isPlaying) {
                    val now = System.currentTimeMillis()
                    val elapsed = now - lastPositionUpdateTime
                    currentPosition += elapsed
                    lastPositionUpdateTime = now
                }
            }
        }, 0, 1) 
    }
    
    fun stopPositionUpdate() {
        positionUpdateTimer?.cancel()
        positionUpdateTimer = null
    }
    
    fun setPosition(position: Long) {
        currentPosition = position
        lastPositionUpdateTime = System.currentTimeMillis()
    }
    
    fun resetPosition() {
        currentPosition = 0L
        lastPositionUpdateTime = System.currentTimeMillis()
    }
}