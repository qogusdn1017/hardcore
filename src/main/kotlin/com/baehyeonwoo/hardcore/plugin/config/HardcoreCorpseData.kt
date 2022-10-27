/*
 * Copyright (c) 2022 Komworld Dev Team
 *
 *  Licensed under the General Public License, Version 3.0. (https://opensource.org/licenses/gpl-3.0/)
 */

package com.baehyeonwoo.hardcore.plugin.config

import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject
import com.baehyeonwoo.hardcore.plugin.objects.HardcoreObject.server
import io.github.monun.tap.fake.FakeEntity
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author Komworld Dev Team
 */

@Suppress("UNUSED", "UNCHECKED_CAST")
data class HardcoreCorpseData(
    val location: Location,
    val uniqueId: UUID?,
    val inventory: Inventory,
    val name: String,
): ConfigurationSerializable {
    companion object {
        fun from(fakeEntity: FakeEntity<Player>, uuid: UUID? = null): HardcoreCorpseData {
            val location = fakeEntity.location
            return HardcoreCorpseData(location, uuid, HardcoreObject.linkedInventory[fakeEntity.bukkitEntity.uniqueId]!!, fakeEntity.bukkitEntity.name)
        }

        @JvmStatic
        fun deserialize(args: Map<String, Any>): HardcoreCorpseData {
            val location = args["location"] as Location
            val uuid = UUID.fromString(args["uniqueId"] as String)
            val name = args["name"] as String

            val inventoryContents = server.createInventory(null, 45, text(name)).apply {
                contents = (args["inventory"] as List<ItemStack>).toTypedArray()
            }

            return HardcoreCorpseData(location, uuid, inventoryContents, name)
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val out = mutableMapOf<String, Any>()
        out["location"] = location
        uniqueId?.let { out["uniqueId"] = it.toString() }
        out["inventory"] = inventory.contents.toList()
        out["name"] = name
        return out
    }
}