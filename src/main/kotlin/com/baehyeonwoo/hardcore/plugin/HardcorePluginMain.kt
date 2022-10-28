/*
 * Copyright (c) 2022 Komworld Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.hardcore.plugin

import com.baehyeonwoo.hardcore.plugin.commands.HardcoreKommand.gameKommand
import com.baehyeonwoo.hardcore.plugin.commands.HardcoreKommand.unbanKommand
import com.baehyeonwoo.hardcore.plugin.config.HardcoreCorpseData
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.coroutines
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.corpses
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.fakeServer
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.isRunning
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.start
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.usableUnbans
import io.github.monun.kommand.kommand
import org.bukkit.configuration.serialization.ConfigurationSerialization.registerClass
import org.bukkit.plugin.java.JavaPlugin

/**
 * @author Komworld Dev Team
 */

@Suppress("UNCHECKED_CAST")
class HardcorePluginMain : JavaPlugin() {

    companion object {
        lateinit var instance: HardcorePluginMain
            private set
    }

    override fun onEnable() {
        instance = this

        registerClass(HardcoreCorpseData::class.java)
        corpses.addAll(config.getList("corpses", listOf<HardcoreCorpseData>()) as List<HardcoreCorpseData>)

        if (config.getBoolean("isRunning")) {
            logger.warning("기존에 게임 실행 상태로 서버를 종료하여 게임이 계속 실행됩니다. 게임 종료를 제외하여 \"/hardcore\" 명령어를 다시 입력하실 필요가 없습니다.")
            start()
        }

        kommand {
            register("hardcore") {
                requires { isConsole }
                gameKommand(this)
            }
            register("unban") {
                requires { isPlayer }
                unbanKommand(this)
            }
        }
    }

    override fun onDisable() {
        coroutines.forEach { it.cancel() }
        server.onlinePlayers.forEach { fakeServer.removePlayer(it) }

        if (isRunning) config.set("isRunning", true) else config.set("isRunning", false)
        config.set("usableUnbans", usableUnbans)
        config.set("corpses", corpses.toList())
        saveConfig()
    }
}