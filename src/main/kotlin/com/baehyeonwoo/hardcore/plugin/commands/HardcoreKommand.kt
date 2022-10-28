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
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.usableUnbans
import io.github.monun.kommand.getValue
import io.github.monun.kommand.node.LiteralNode
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.BanList
import org.bukkit.OfflinePlayer

/**
 * @author Komworld Dev Team
 */

object HardcoreKommand {
    fun gameKommand(builder: LiteralNode) {
        builder.apply {
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
                        player.sendMessage(text("완료"))
                        --usableUnbans
                        if (usableUnbans == 0) unbanable = false
                    }
                    else {
                        player.sendMessage(text("조건을 만족하지 못하여 명령어를 사용할 수 없습니다.", NamedTextColor.RED))
                    }
                }
            }
        }
    }
}