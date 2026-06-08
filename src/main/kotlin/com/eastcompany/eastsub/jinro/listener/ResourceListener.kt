package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.Jinro
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ResourceListener(private val plugin: Jinro) : Listener {

    private val managerKey = NamespacedKey(plugin, "resource_manager_spawned")

    /**
     * 1. 鉄鉱石や木ブロックが破壊（左クリック長押し）された時の処理
     */
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val material = block.type

        if (material != Material.IRON_ORE && material != Material.STRIPPED_OAK_WOOD) return

        event.isDropItems = false

        val player = event.player

        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F)
        player.inventory.addItem(ItemStack(Material.EMERALD, 1))

        val centerLoc = block.location.clone().add(0.5, 0.5, 0.5)

        block.world.getNearbyEntities(centerLoc, 0.6, 0.6, 0.6) { it is ItemDisplay }.forEach { entity ->
            if (entity.persistentDataContainer.has(managerKey, PersistentDataType.BOOLEAN)) {
                entity.remove()
            }
        }
    }

    /**
     * 2. Interaction（チェストや花）を「右クリック」した時の処理
     */
    @EventHandler
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        val clickedEntity = event.rightClicked
        if (clickedEntity !is Interaction) return

        val processed = handleInteractionPickup(event.player, clickedEntity)
        if (processed) {
            event.isCancelled = true
        }
    }

    /**
     * 3. Interaction（チェストや花）を「左クリック（パンチ）」した時の処理
     */
    @EventHandler
    fun onPlayerAttackEntity(event: PrePlayerAttackEntityEvent) {
        val clickedEntity = event.attacked
        if (clickedEntity !is Interaction) return

        handleInteractionPickup(event.player, clickedEntity)
        event.isCancelled = true
    }

    /**
     * Interactionをクリック・パンチした際の共通回収ロジック
     * @return 処理が行われた場合は true
     */
    private fun handleInteractionPickup(player: Player, interaction: Interaction): Boolean {
        if (!interaction.persistentDataContainer.has(managerKey, PersistentDataType.BOOLEAN)) return false
        if (!interaction.isValid) return false

        val loc = interaction.location
        val block = loc.block
        val world = loc.world ?: return false

        // 💡 追加: 演出用にInteractionの中心（あるいはブロックの中心）の座標を定義
        val centerLoc = loc.clone().add(0.0, 0.3, 0.0)

        // 1. プレイヤーのインベントリにエメラルドを追加
        player.playSound(player.location, Sound.ENTITY_ITEM_PICKUP, 1.0F, 1.0F)
        player.inventory.addItem(ItemStack(Material.EMERALD, 1))

        // 💡 追加: 破壊対象に応じた独自の音とパーティクル処理
        if (block.type == Material.CORNFLOWER) {
            // 花の場合：草ブロックの破壊音とブロック破壊パーティクルを再生
            world.playSound(centerLoc, Sound.BLOCK_GRASS_BREAK, 1.0F, 1.0F)
            world.spawnParticle(Particle.BLOCK, centerLoc, 20, 0.2, 0.2, 0.2, 0.0, Material.CORNFLOWER.createBlockData())

            // 花のブロック自体を消去
            block.type = Material.AIR
        } else {
            // 花以外のInteraction＝チェストと判定し、チェストを開ける音を再生
            world.playSound(centerLoc, Sound.BLOCK_CHEST_OPEN, 1.0F, 1.0F)
        }

        // 2. Interaction 自体を撤去
        interaction.remove()

        // 3. 同ブロック内にある ItemDisplay（マーカーやチェスト）を撤去
        world.getNearbyEntities(loc, 0.6, 0.6, 0.6) { it is ItemDisplay }.forEach { entity ->
            if (entity.persistentDataContainer.has(managerKey, PersistentDataType.BOOLEAN)) {
                entity.remove()
            }
        }

        return true
    }
}