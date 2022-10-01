/*
 * Copyright (c) 2022 HDB
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.shareadvancement.plugin

import com.baehyeonwoo.shareadvancement.plugin.commands.ShareAdvancementKommand.register
import com.baehyeonwoo.shareadvancement.plugin.config.CorpseData
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.corpses
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.fakeServer
import io.github.monun.kommand.kommand
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Komworld Dev Team
 */

class ShareAdvancementPluginMain : JavaPlugin() {

    companion object {
        lateinit var instance: ShareAdvancementPluginMain
            private set
    }

    override fun onEnable() {
        instance = this

        ConfigurationSerialization.registerClass(CorpseData::class.java)
        corpses.addAll(config.getList("corpses", listOf<CorpseData>()) as List<CorpseData>)

        kommand {
            register("share") {
                requires { isOp }
                register(this)
            }
        }

        server.onlinePlayers.forEach {
            fakeServer.addPlayer(it)
        }
    }

    override fun onDisable() {
        server.onlinePlayers.forEach {
            fakeServer.removePlayer(it)
        }

        config.set("corpses", corpses.toList())
        saveConfig()
    }
}