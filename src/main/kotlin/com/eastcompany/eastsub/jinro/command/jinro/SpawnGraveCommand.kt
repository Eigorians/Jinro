package com.eastcompany.eastsub.jinro.command.jinro

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

class SpawnGraveCommand : SubCommand {

    override val name: String = "spawngrave"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .requires { it.sender is Player }
            .executes { context ->
                val player = context.source.sender as Player
                spawnGraveVisual(player)
                1
            }
    }

    private fun spawnGraveVisual(player: Player) {
        val loc = player.location.apply {
            pitch = 0f
        }
        val defaultRotation = Quaternionf(0f, 0f, 0f, 1f)

        // データの永続化用キーを定義
        val graveKey = NamespacedKey.fromString("werewolf:grave")!!
        val ownerKey = NamespacedKey.fromString("werewolf:owner")!!

        // この墓全体を識別するためのランダムな一意のUUID
        val graveId = UUID.randomUUID().toString()
        val ownerId = player.uniqueId.toString()

        // 1. Interaction (当たり判定) の生成
        loc.world.spawn(loc, Interaction::class.java) { ent ->
            ent.interactionWidth = 0.6f
            ent.interactionHeight = 0.6f
            ent.isResponsive = true

            // データコンテナへ書き込み
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }

        // 2. 土台 (grave_stone) の生成
        val stoneLoc = loc.clone().add(0.0, 0.329, 0.0)
        loc.world.spawn(stoneLoc, ItemDisplay::class.java) { ent ->
            ent.setItemStack(ItemStack(Material.DIRT).apply {
                val modelKey = NamespacedKey.fromString("minecraft:grave")
                itemMeta = itemMeta.apply { setItemModel(modelKey) }
            })
            ent.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                defaultRotation,
                Vector3f(0.67f, 0.67f, 0.67f),
                defaultRotation
            )

            // データコンテナへ書き込み（UUIDでの紐づけ）
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }

        // 3. 頭部 (grave_head) の生成
        val headLoc = loc.clone().apply {
            y += 0.245
            yaw += -180f
        }
        loc.world.spawn(headLoc, ItemDisplay::class.java) { ent ->
            ent.setItemStack(ItemStack(Material.PLAYER_HEAD).apply {
                itemMeta = (itemMeta as? SkullMeta)?.apply {
                    owningPlayer = player
                }
            })
            ent.transformation = Transformation(
                Vector3f(0f, 0f, 0f),
                defaultRotation,
                Vector3f(0.33f, 0.33f, 0.33f),
                defaultRotation
            )

            // データコンテナへ書き込み（UUIDでの紐づけ）
            ent.persistentDataContainer.set(graveKey, PersistentDataType.STRING, graveId)
            ent.persistentDataContainer.set(ownerKey, PersistentDataType.STRING, ownerId)
        }
    }
}