package com.eastcompany.eastsub.jinro.game

import com.eastcompany.eastsub.jinro.Jinro
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Display
import org.bukkit.entity.Interaction
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.random.Random

class ResourceManager(private val plugin: Jinro) {

    private val spawnedEntities = mutableListOf<UUID>()
    private val placedBlockLocations = mutableListOf<Location>()

    private val managerKey = NamespacedKey(plugin, "resource_manager_spawned")

    /**
     * 資源をランダムに選んで生成するメインメソッド
     */
    fun spawnRandomResources(amount: Int) {
        clearAllResources()

        val currentMap = plugin.configManager.gameConfig.selectedMap
        val mapData = plugin.configManager.gameConfig.mapData[currentMap] ?: return

        // 1. 花の生成 (Cornflower ブロック + Interaction)
        mapData.resourceFlowers.shuffled().take(amount).forEach { loc ->
            val block = loc.block
            block.type = Material.CORNFLOWER
            placedBlockLocations.add(block.location)
            spawnFlowerInteraction(loc)
            spawnMarkerDisplay(loc, Material.IRON_ORE, "minecraft:other/select_block_small")
        }

        // 2. 鉄鉱石の生成 (鉄ブロック + 全員が見えるItemDisplayマーカー)
        mapData.resourceIrons.shuffled().take(amount).forEach { loc ->
            val block = loc.block
            block.type = Material.IRON_ORE
            placedBlockLocations.add(block.location)
            spawnMarkerDisplay(loc, Material.IRON_ORE, "minecraft:other/select_block")
        }

        // 3. 木の生成 (皮を剥いだアカシアの原木 + 見た目用のオークの原木Display + マーカー)
        mapData.resourceWoods.shuffled().take(amount).forEach { loc ->
            val block = loc.block
            // 💡 内部のブロックを STRIPPED_ACACIA_LOG に変更
            block.type = Material.STRIPPED_OAK_WOOD
            placedBlockLocations.add(block.location)

            // 💡 見た目をオークの原木にするダミーDisplayを生成
            spawnDummyOAKDisplay(loc)
            // 💡 マーカー用（既存処理）のアイテムモデルを指定
            spawnMarkerDisplay(loc, Material.OAK_LOG, "minecraft:other/select_block")
        }

        // 4. チェストの生成 (ItemDisplay + Interaction)
        mapData.resourceChests.shuffled().take(amount).forEach { loc ->
            spawnActiveChest(loc)
        }
    }

    /**
     * 木ブロックの見た目をOAK_LOGに偽装するためのItemDisplayを生成
     */
    private fun spawnDummyOAKDisplay(loc: Location) {
        val world = loc.world ?: return

        // ブロックの中心に座標を合わせる
        val spawnLoc = loc.clone().apply {
            x = blockX + 0.5
            y = blockY + 0.5
            z = blockZ + 0.5
            yaw = 0f
            pitch = 0f
        }

        world.spawn(spawnLoc, ItemDisplay::class.java) { itemDisplay ->
            // 見た目はオークの原木
            itemDisplay.setItemStack(ItemStack(Material.OAK_LOG))

            // ブロックとして綺麗に表示するための設定
            itemDisplay.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.NONE
            itemDisplay.viewRange = 40.0f // マーカーより広め（必要に応じて調整）
            itemDisplay.brightness = Display.Brightness(15, 15) // 暗くならないように最大輝度にするのがおすすめ

            // 元のブロックの黒い隙間やチカチカ（Z-Fighting）を防ぐため、1.001倍より少しだけ大きく設定
            val scale = Vector3f(1.0005f, 1.0005f, 1.0005f)
            val translation = Vector3f(0.0f, 0.0f, 0.0f)
            val leftRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
            val rightRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
            itemDisplay.transformation = Transformation(translation, leftRotation, scale, rightRotation)

            // 管理用タグの設定
            itemDisplay.persistentDataContainer.set(managerKey, PersistentDataType.BOOLEAN, true)
            spawnedEntities.add(itemDisplay.uniqueId)
        }
    }

    /**
     * 鉄鉱石・木ブロック・花用のマーカー（全員に表示）
     */
    private fun spawnMarkerDisplay(loc: Location, material: Material, model: String) {
        val world = loc.world ?: return

        val spawnLoc = loc.clone().apply {
            x = blockX + 0.5
            y = blockY + 0.5
            z = blockZ + 0.5
            yaw = 0f
            pitch = 0f
        }

        world.spawn(spawnLoc, ItemDisplay::class.java) { itemDisplay ->
            val item = ItemStack(material).apply {
                itemMeta = itemMeta?.apply {
                    NamespacedKey.fromString(model)?.let { modelKey ->
                        this.itemModel = modelKey
                    }
                }
            }
            itemDisplay.setItemStack(item)
            itemDisplay.viewRange = 10.0f
            itemDisplay.brightness = Display.Brightness(10, 10)

            val translation = Vector3f(0.0f, 0.0f, 0.0f)
            val leftRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
            val rightRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
            val scale = Vector3f(1.001f, 1.001f, 1.001f)
            itemDisplay.transformation = Transformation(translation, leftRotation, scale, rightRotation)

            itemDisplay.persistentDataContainer.set(managerKey, PersistentDataType.BOOLEAN, true)
            spawnedEntities.add(itemDisplay.uniqueId)
        }
    }

    private fun spawnFlowerInteraction(loc: Location) {
        val world = loc.world ?: return
        val centerLoc = loc.clone().apply {
            x = blockX + 0.5
            y = blockY + 0.1
            z = blockZ + 0.5
            yaw = 0f
            pitch = 0f
        }
        world.spawn(centerLoc, Interaction::class.java) { interaction ->
            interaction.interactionWidth = 0.6f
            interaction.interactionHeight = 0.6f
            interaction.persistentDataContainer.set(managerKey, PersistentDataType.BOOLEAN, true)
            spawnedEntities.add(interaction.uniqueId)
            interaction.isResponsive = true
        }
    }

    private fun spawnActiveChest(loc: Location) {
        val world = loc.world ?: return
        val centerLoc = loc.clone().apply {
            x = blockX + 0.5
            y = blockY + 0.5
            z = blockZ + 0.5
            yaw = Random.nextFloat() * 360f
            pitch = 0f
        }

        world.spawn(centerLoc, ItemDisplay::class.java) { display ->
            display.setItemStack(ItemStack(Material.CHEST))
            val scale = Vector3f(0.6001f, 0.6001f, 0.6001f)
            val translation = Vector3f(0f, -0.3f, 0f)
            val leftRot = Quaternionf()
            val rightRot = Quaternionf()
            display.transformation = Transformation(translation, leftRot, scale, rightRot)

            display.persistentDataContainer.set(managerKey, PersistentDataType.BOOLEAN, true)
            spawnedEntities.add(display.uniqueId)
        }

        val spawn = centerLoc.clone()
        spawn.y -= 0.5
        world.spawn(spawn, Interaction::class.java) { interaction ->
            interaction.interactionWidth = 0.6f
            interaction.interactionHeight = 0.6f
            interaction.persistentDataContainer.set(managerKey, PersistentDataType.BOOLEAN, true)
            spawnedEntities.add(interaction.uniqueId)
            interaction.isResponsive = true
        }
    }

    fun clearAllResources() {
        placedBlockLocations.forEach { loc ->
            val block = loc.block
            // 💡 削除判定対象に STRIPPED_ACACIA_LOG も追加
            if (block.type == Material.CORNFLOWER || block.type == Material.IRON_ORE || block.type == Material.OAK_LOG || block.type == Material.STRIPPED_OAK_WOOD) {
                block.type = Material.AIR
            }
        }
        placedBlockLocations.clear()

        spawnedEntities.forEach { uuid ->
            val entity = plugin.server.getEntity(uuid)
            if (entity != null && entity.isValid) {
                entity.remove()
            }
        }
        spawnedEntities.clear()

        val currentMap = plugin.configManager.gameConfig.selectedMap
        val mapData = plugin.configManager.gameConfig.mapData[currentMap]

        val worlds = mutableSetOf<org.bukkit.World>()
        mapData?.resourceChests?.mapNotNull { it.world }?.let { worlds.addAll(it) }
        mapData?.resourceFlowers?.mapNotNull { it.world }?.let { worlds.addAll(it) }
        mapData?.resourceIrons?.mapNotNull { it.world }?.let { worlds.addAll(it) }
        mapData?.resourceWoods?.mapNotNull { it.world }?.let { worlds.addAll(it) }

        worlds.forEach { world ->
            world.entities.forEach { entity ->
                if (entity.persistentDataContainer.has(managerKey, PersistentDataType.BOOLEAN)) {
                    entity.remove()
                }
            }
        }
    }
}