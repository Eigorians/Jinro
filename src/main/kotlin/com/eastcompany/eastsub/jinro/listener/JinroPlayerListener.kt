package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.constant.JinroKeys
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import com.eastcompany.eastsub.jinro.manager.JinroTimeManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent // 💡 追加
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

class JinroPlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        JinroTimeManager.getBossBar()?.addPlayer(player)
    }

    // 💡 追加: 試合中に特定の役職アイテムを捨てるのをキャンセルする
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!JinroGameManager.isGameRunning) return

        val itemStack = event.itemDrop.itemStack
        val meta = itemStack.itemMeta ?: return

        // 💡 1行でスッキリ判定できるようになります！
        if (meta.persistentDataContainer.has(JinroKeys.ITEM_TAG, PersistentDataType.STRING)) {
            event.isCancelled = true
        }
    }
    @EventHandler
    fun onPlayerClickInventory(event: InventoryClickEvent) {
        if (!JinroGameManager.isGameRunning) return

        val itemStack = event.currentItem ?: return
        val meta = itemStack.itemMeta ?: return

        // 💡 1行でスッキリ判定できるようになります！
        if (meta.persistentDataContainer.has(JinroKeys.ITEM_TAG, PersistentDataType.STRING)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (!JinroGameManager.isGameRunning) return

        val player = event.entity
        val uuid = player.uniqueId
        val gamePlayer = JinroGameManager.gamePlayers[uuid] ?: return

        if (!gamePlayer.isAlive) return

        gamePlayer.isAlive = false
        JinroGameManager.activeSpectators.add(uuid)
        event.isCancelled = true

        spawnGraveVisual(player)
        player.gameMode = GameMode.SPECTATOR
        event.deathMessage(null)

        GameEndChecker(JinroGameManager)
    }

    private fun spawnGraveVisual(player: Player) {
        val loc = getGroundLocation(player.location).apply { pitch = 0f }
        val defaultRotation = Quaternionf(0f, 0f, 0f, 1f)

        val graveKey = NamespacedKey.fromString("werewolf:grave") ?: return
        val ownerKey = NamespacedKey.fromString("werewolf:owner") ?: return
        val graveId = UUID.randomUUID().toString()
        val ownerId = player.uniqueId.toString()

        loc.world.spawn(loc, Interaction::class.java) { ent ->
            ent.interactionWidth = 0.6f
            ent.interactionHeight = 0.6f
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }

        val stoneLoc = loc.clone().add(0.0, 0.329, 0.0)
        loc.world.spawn(stoneLoc, ItemDisplay::class.java) { ent ->
            ent.setItemStack(ItemStack(Material.DIRT).apply {
                val modelKey = NamespacedKey.fromString("minecraft:other/grave")
                itemMeta = itemMeta.apply { setItemModel(modelKey) }
            })
            ent.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                defaultRotation,
                Vector3f(0.67f, 0.67f, 0.67f),
                defaultRotation
            )
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }

        val headLoc = loc.clone().apply {
            y += 0.245
            yaw += -180f
        }
        loc.world.spawn(headLoc, ItemDisplay::class.java) { ent ->
            ent.setItemStack(ItemStack(Material.PLAYER_HEAD).apply {
                itemMeta = (itemMeta as? SkullMeta)?.apply { owningPlayer = player }
            })
            ent.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                defaultRotation,
                Vector3f(0.33f, 0.33f, 0.33f),
                defaultRotation
            )
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }
    }

    private fun getGroundLocation(startLoc: Location): Location {
        val world = startLoc.world
        val cloneLoc = startLoc.clone()

        while (cloneLoc.blockY >= world.minHeight) {
            val block = cloneLoc.block
            if (!block.type.isAir && block.type.isSolid) {
                return cloneLoc.add(0.0, 1.0, 0.0)
            }
            cloneLoc.add(0.0, -1.0, 0.0)
        }
        return startLoc
    }

    @EventHandler
    fun onEntityDamage(event: org.bukkit.event.entity.EntityDamageEvent) {
        if (event.entity !is Player) return
        if (JinroGameManager.isGameRunning && JinroGameManager.currentPhase == com.eastcompany.eastsub.jinro.game.JinroPhase.COURT) {
            if (event.cause == org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM) return
            event.damage = 0.0
        }
    }
    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        if (!JinroGameManager.isGameRunning) return
        event.isCancelled = true
    }
}