package com.baehyeonwoo.shareadvancement.plugin.config

import com.baehyeonwoo.shareadvancement.plugin.objects.ShareAdvancementObject
import io.github.monun.tap.fake.FakeEntity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import java.util.*

data class CorpseData(
    val location: Location,
    val uniqueId: UUID?,
    val inventory: Inventory,
    val name: String,
): ConfigurationSerializable {
    companion object {

        fun from(fakeEntity: FakeEntity<Player>, uuid: UUID? = null): CorpseData {
            val location = fakeEntity.location
            return CorpseData(location, uuid, ShareAdvancementObject.linkedInventory[fakeEntity.bukkitEntity.uniqueId]!!, fakeEntity.bukkitEntity.name)
        }

        @Suppress("UNUSED", "UNCHECKED_CAST")
        @JvmStatic
        fun deserialize(args: Map<String, Any>): CorpseData {
            val location = args["location"] as Location
            val uuid = UUID.fromString(args["uniqueId"] as String)
            val name = args["name"] as String

            val inventoryContents = ShareAdvancementObject.server.createInventory(null, 45, Component.text("")).apply {
                contents = (args["inventory"] as List<ItemStack>).toTypedArray()
            }

            return CorpseData(location, uuid, inventoryContents, name)
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        val out = mutableMapOf<String, Any>()
        out["location"] = location
        uniqueId?.let { out["uniqueId"] = it.toString() }
        out["inventory"] = inventory.contents?.toList()!!
        out["name"] = name
        return out
    }
}