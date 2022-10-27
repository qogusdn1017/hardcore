/*
 * Copyright (c) 2022 Komworld Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.hardcore.plugin.commands

import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.isRunning
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.plugin
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.server
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.start
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.stop
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.unbanable
import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import net.kyori.adventure.text.Component.text
import org.bukkit.BanList
import org.bukkit.OfflinePlayer

/**
 * @author Komworld Dev Team
 */

object HardcoreKommand {
    fun gameKommand(builder: LiteralNode) {
        builder.apply {
            requires { isOp }
            executes {
                if (!isRunning) {
                    plugin.logger.info("게임 시작")
                    start()
                } else {
                    plugin.logger.info("게임 종료")
                    stop()
                }
            }
        }
    }

    fun unbanKommand(builder: LiteralNode) {
        builder.apply {
            val bannedPlayers = dynamic { _, input ->
                server.getOfflinePlayer(input)
            }.apply {
                suggests {
                    val players = server.bannedPlayers.map { player -> player.name.toString() }
                    suggest(players) {
                        text(it)
                    }
                }
            }


            then("target" to bannedPlayers) {
                executes {
                    if (unbanable) {
                        val target: OfflinePlayer by it

                        target.name?.let { name -> server.getBanList(BanList.Type.NAME).pardon(name) }
                        sender.sendMessage(text("완료"))
                        unbanable = false
                    }
                }
            }
        }
    }
}