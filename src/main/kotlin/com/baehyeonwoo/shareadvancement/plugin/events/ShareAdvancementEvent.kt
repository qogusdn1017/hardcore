/*
 * Copyright (c) 2022 HDB
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.shareadvancement.plugin.events

import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.createCorpseNPC
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.fakePlayers
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.fakeServer
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.openInventory
import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject.server
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.PlayerInfoAction
import io.github.monun.tap.protocol.PacketSupport
import io.github.monun.tap.protocol.sendPacket
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

object ShareAdvancementEvent : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        fakeServer.addPlayer(player)
        fakeServer.entities.filter { it.bukkitEntity is Player }.map {
            @Suppress("UNCHECKED_CAST")
            it as FakeEntity<Player>
            PacketSupport.playerInfoAction(PlayerInfoAction.REMOVE, it.bukkitEntity)
        }.forEach {
            player.sendPacket(it)
        }
        joinMessage(null)
    }


    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        quitMessage(null)
    }

    @EventHandler
    fun PlayerAdvancementDoneEvent.onAdvancementDone() {
        server.onlinePlayers.forEach {
            val key = advancement.key

            if (!key.toString().startsWith("minecraft:recipes") && !advancement.key.toString().endsWith("root")) {
                val progress = it.getAdvancementProgress(advancement)
                advancement.criteria.forEach { criterion ->
                    progress.awardCriteria(criterion)
                }
            }
        }
    }

    @EventHandler
    fun AsyncChatEvent.onChat() {
        if (!player.isOp) isCancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        createCorpseNPC(player, player.location.apply {
            pitch = 0f
            yaw = 0f

            while (block.type.isAir) {
                y -= 0.05
            }
        })
        player.inventory.clear()
        player.banPlayer(" ")
        deathMessage(null)
    }


    @EventHandler(ignoreCancelled = true)
    fun PlayerToggleSneakEvent.onToggleSneak() {
        if (isSneaking) {
            fakePlayers.find {
                it.bukkitEntity.location.distance(player.location) <= 0.67 ||
                it.bukkitEntity.location.clone().subtract(1.0, 0.0, 0.0).distance(player.location) <= 0.67 ||
                it.bukkitEntity.location.clone().subtract(2.0, 0.0, 0.0).distance(player.location) <= 0.67
            }?.let {
                openInventory(player, it)
            }
        }
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onCommandPreProcess() {
        if (!player.isOp) isCancelled = true
    }
}