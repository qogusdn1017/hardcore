/*
 * Copyright (c) 2022 Komworld Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.shareadvancement.plugin.objects

import com.baehyeonwoo.shareadvancement.plugin.ShareAdvancementPluginMain
import com.baehyeonwoo.shareadvancement.plugin.config.CorpseData
import com.baehyeonwoo.shareadvancement.plugin.events.ShareAdvancementEvent
import io.github.monun.heartbeat.coroutines.HeartbeatScope
import io.github.monun.tap.fake.FakeEntity
import io.github.monun.tap.fake.FakeEntityServer
import io.github.monun.tap.mojangapi.MojangAPI
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket
import net.minecraft.world.entity.Pose
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.random.Random.Default.nextInt

/**
 * @author Komworld Dev Team
 */

@Suppress("UNCHECKED_CAST")
object ShareAdvancementObject {
    val plugin = ShareAdvancementPluginMain.instance
    val server = plugin.server
    val fakeServer = FakeEntityServer.create(plugin)
    val fakePlayers get() = fakeServer.entities.filter { it.bukkitEntity is Player } as List<FakeEntity<Player>>
    var isRunning = false
    val linkedInventory = HashMap<UUID, Inventory>()
    val corpses = arrayListOf<CorpseData>()

    fun start() {
        isRunning = true
        server.worlds.forEach {
            it.setGameRule(GameRule.KEEP_INVENTORY, true)
            it.setGameRule(GameRule.REDUCED_DEBUG_INFO, true)

            it.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            it.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        }
        server.onlinePlayers.forEach {
            fakeServer.addPlayer(it)
        }
        server.pluginManager.registerEvents(ShareAdvancementEvent, plugin)

        server.scheduler.runTaskTimer(plugin, Runnable {
            fakeServer.update()
        }, 0, 1)

        println(corpses.size)
        corpses.forEach {
            createCorpseNpcAsData(it, true)
        }
    }

    fun stop() {
        isRunning = false
        fakePlayers.forEach { it.remove() }
        HandlerList.unregisterAll(ShareAdvancementEvent)
        server.scheduler.cancelTasks(plugin)
    }

    fun createCorpseNPC(player: Player, deathLocation: Location) {
        val data = CorpseData(deathLocation, player.uniqueId, createCorpseInventory(player), player.name)
        createCorpseNpcAsData(data)
    }

    fun createCorpseNpcAsData(corpseData: CorpseData, isLoaded: Boolean = false, skinProfile: MojangAPI.SkinProfile? = null) {
        server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val profile = skinProfile ?: corpseData.uniqueId?.let { MojangAPI.fetchSkinProfile(it) }!!
            server.scheduler.runTask(plugin, Runnable {
                val npc = fakeServer.spawnPlayer(
                    corpseData.location,
                    "${profile.name}의 시체",
                    profile.profileProperties().toSet()
                )

                npc.updateMetadata {
                    (this as CraftPlayer).handle.apply {
                        pose = Pose.SLEEPING
                    }
                    linkedInventory[uniqueId] = corpseData.inventory
                }
                if (!isLoaded) corpses += CorpseData.from(npc, corpseData.uniqueId)

                HeartbeatScope().launch {
                    delay(150L)
                    server.onlinePlayers.forEach {
                        fakePlayers.forEach { selected ->
                            (it as CraftPlayer).handle.connection.send(ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, (selected.bukkitEntity as CraftPlayer).handle))
                        }
                    }
                }
            })
        })
    }


    private fun createCorpseInventory(player: Player) = server.createInventory(null, 45, text(player.name, NamedTextColor.DARK_GRAY)).apply {
        contents = Array(45) { if (it < 41) player.inventory.contents!![it] else ItemStack(Material.AIR) }
    }

    fun openInventory(player: Player, body: FakeEntity<Player>) {
        player.openInventory(linkedInventory[body.bukkitEntity.uniqueId]!!)
        HeartbeatScope().launch {
            delay(60000L)
            // TODO remove player
        }
    }
}