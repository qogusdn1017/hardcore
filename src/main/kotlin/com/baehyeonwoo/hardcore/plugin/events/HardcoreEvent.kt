/*
 * Copyright (c) 2022 Komworld Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.hardcore.plugin.events

import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.createCorpseNPC
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.fakePlayers
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.fakeServer
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.openInventory
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.server
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.unbanable
import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent
import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.papermc.paper.event.player.AsyncChatEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

/**
 * @author Komworld Dev Team
 */

object HardcoreEvent : Listener {
    @EventHandler
    fun PlayerJoinEvent.onJoin() {
        HeartbeatScope().launch {
            delay(1000L)
            fakeServer.addPlayer(player)
        }
        joinMessage(null)

        server.onlinePlayers.filter { player.uniqueId != it.uniqueId }.forEach { otherP ->
            server.advancementIterator().forEach { advancement ->
                otherP.getAdvancementProgress(advancement).awardedCriteria.forEach { criterion ->
                    player.getAdvancementProgress(advancement).awardCriteria(criterion)
                }
            }
        }
    }

    @EventHandler
    fun PlayerQuitEvent.onQuit() {
        fakeServer.removePlayer(player)
        quitMessage(null)
    }

    @EventHandler
    fun PlayerAdvancementCriterionGrantEvent.onCriterionGrant() {
        server.onlinePlayers.forEach {
            it.getAdvancementProgress(advancement).awardCriteria(criterion)

            if (it.getAdvancementProgress(advancement).isDone) {
                if (!unbanable) unbanable = true
            }
        }
    }

    @EventHandler
    fun AsyncChatEvent.onChat() {
        if (!player.isOp) isCancelled = true
    }

    @EventHandler
    fun PlayerDeathEvent.onDeath() {
        createCorpseNPC(player, player.location.clone().apply {
            pitch = 0f
            yaw = 0f

            while (block.type.isAir) { y -= 0.025 }
        })
        player.inventory.clear()
        player.banPlayer(" ")
        deathMessage(null)
    }


    @EventHandler(ignoreCancelled = true)
    fun PlayerToggleSneakEvent.onToggleSneak() {
        if (isSneaking) {
            fakePlayers.find {
                it.bukkitEntity.location.distance(player.location) <= 1.5 ||
                it.bukkitEntity.location.clone().subtract(1.0, 0.0, 0.0).distance(player.location) <= 1.5 ||
                it.bukkitEntity.location.clone().subtract(2.0, 0.0, 0.0).distance(player.location) <= 1.5
            }?.let {
                openInventory(player, it)
            }
        }
    }

    @EventHandler
    fun PlayerCommandPreprocessEvent.onCommandPreProcess() {
        if (!player.isOp) isCancelled = true
    }

    @EventHandler
    fun PaperServerListPingEvent.onServerListPing() {
        setHidePlayers(true)
        motd(text("HARDCORE", NamedTextColor.RED).decorate(TextDecoration.BOLD))
    }
}