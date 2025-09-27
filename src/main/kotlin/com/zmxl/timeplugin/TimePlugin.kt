package com.zmxl.timeplugin

import org.pf4j.Plugin
import org.pf4j.PluginWrapper

class TimePlugin(wrapper: PluginWrapper) : Plugin(wrapper) {

    override fun start() {
        println("SaltTimePlugin started")
    }

    override fun stop() {
        println("SaltTimePlugin stopped")
    }
}