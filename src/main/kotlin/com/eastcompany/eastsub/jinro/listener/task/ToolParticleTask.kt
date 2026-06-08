package com.eastcompany.eastsub.jinro.listener.task

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.constant.JinroKeys
import net.kyori.adventure.text.Component
import org.bukkit.*
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

class ToolParticleTask(private val plugin: Jinro) : BukkitRunnable(), Listener {

    // メモリリーク防止のため、Entityオブジェクトの直接保持によって確実に削除可能にする
    private val activeDisplays = mutableMapOf<UUID, MutableList<Entity>>()
    private val currentTrackingMaterial = mutableMapOf<UUID, Material>()

    init {
        // イベントリスナーを登録（ログアウト・ワールド変更対策）
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    override fun run() {
        val config = plugin.configManager.gameConfig

        for (player in Bukkit.getOnlinePlayers()) {
            val pUuid = player.uniqueId
            val item = player.inventory.itemInMainHand
            val mapName = getToolMapName(item)

            // ツールを持っていない、または有効なマップデータがない場合
            if (mapName == null) {
                if (activeDisplays.containsKey(pUuid)) {
                    clearPlayerDisplays(player)
                }
                continue
            }

            val mapData = config.mapData[mapName] ?: continue

            // アイテムタイプが変わっていない場合はスキップ（余計な計算をスキップ）
            val lastMaterial = currentTrackingMaterial[pUuid]
            if (lastMaterial == item.type) {
                continue
            }

            // 必要な要素をリスト化
            val requiredElements = mutableListOf<Triple<Location, Material, Boolean>>()

            when (item.type) {
                Material.WHITE_WOOL -> {
                    mapData.lobby?.let { loc -> requiredElements.add(Triple(loc, Material.WHITE_WOOL, true)) }
                }
                Material.BLAZE_ROD -> {
                    mapData.spawns.forEach { loc -> requiredElements.add(Triple(loc, Material.ORANGE_WOOL, true)) }
                }
                Material.EMERALD -> {
                    mapData.shops.forEach { loc -> requiredElements.add(Triple(loc, Material.EMERALD_BLOCK, true)) }
                }
                Material.IRON_SWORD -> {
                    mapData.court?.let { loc -> requiredElements.add(Triple(loc, Material.IRON_BLOCK, true)) }
                }

                // 💡 各資源ツールごとに、表示させたいブロックの見た目を指定
                Material.POPPY -> {
                    mapData.resourceFlowers.forEach { loc -> requiredElements.add(Triple(loc, Material.POPPY, false)) }
                }
                Material.CHEST -> {
                    mapData.resourceChests.forEach { loc -> requiredElements.add(Triple(loc, Material.CHEST, false)) }
                }
                Material.IRON_ORE -> {
                    mapData.resourceIrons.forEach { loc -> requiredElements.add(Triple(loc, Material.IRON_ORE, false)) }
                }
                Material.OAK_LOG -> {
                    mapData.resourceWoods.forEach { loc -> requiredElements.add(Triple(loc, Material.OAK_LOG, false)) }
                }
                else -> {
                    clearPlayerDisplays(player)
                    continue
                }
            }

            // 表示の更新が必要なため一度クリア
            clearPlayerDisplays(player)
            currentTrackingMaterial[pUuid] = item.type

            val newEntities = mutableListOf<Entity>()
            for ((loc, material, isArmorStand) in requiredElements) {
                if (loc.world != player.world) continue

                val spawnedEntity: Entity = if (isArmorStand) {

                    loc.world.spawn(loc, ArmorStand::class.java) { stand ->
                        stand.setGravity(false)
                        stand.isMarker = true
                        stand.isGlowing = true
                        stand.equipment.helmet = ItemStack(material)
                        stand.equipment.setItemInMainHand(ItemStack(material))
                        player.showEntity(plugin, stand)
                    }
                } else {

                    val spawnLoc = loc.clone().apply {
                        // align xyz (~0.5 ~500.5 ~0.5) の再現
                        x = blockX + 0.5
                        y = blockY + 500.5
                        z = blockZ + 0.5
                    }

                    spawnLoc.world.spawn(spawnLoc, ItemDisplay::class.java) { itemDisplay ->
                        // 1. アイテムメタの設定 (1.21.4以降の itemModel に対応)
                        val item = ItemStack(material).apply {
                            itemMeta = itemMeta?.apply {
                                NamespacedKey.fromString("minecraft:select_block")?.let { modelKey ->
                                    this.itemModel = modelKey
                                }
                            }
                        }
                        itemDisplay.setItemStack(item)

                        itemDisplay.viewRange = 10.0f // view_range:10f

                        itemDisplay.brightness = Display.Brightness(10,10)

                        // 4. トランスフォーメーションの設定
                        // y軸を「-499.999f」して元の位置(地表付近)に見えるようにオフセット
                        val translation = Vector3f(0.0f, -499.999f, 0.0f)
                        val leftRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
                        val rightRotation = Quaternionf(0.0f, 0.0f, 0.0f, 1.0f)
                        val scale = Vector3f(1.0f, 1.0f, 1.0f)

                        itemDisplay.transformation = Transformation(translation, leftRotation, scale, rightRotation)

                        // 5. 特定のプレイヤーにのみ表示
                        itemDisplay.setVisibleByDefault(false)
                        player.showEntity(plugin, itemDisplay)
                    }

                }
                newEntities.add(spawnedEntity)
            }
            activeDisplays[pUuid] = newEntities
        }
    }

    private fun clearPlayerDisplays(player: Player) {
        val pUuid = player.uniqueId
        val entities = activeDisplays.remove(pUuid) ?: return

        for (entity in entities) {
            if (entity.isValid) {
                entity.remove()
            }
        }
        currentTrackingMaterial.remove(pUuid)
    }

    /**
     * 特定のプレイヤーの表示キャッシュを強制クリアし、次回のタスク実行時に再描画を要求する
     */
    fun forceRefresh(player: Player) {
        clearPlayerDisplays(player)
        currentTrackingMaterial.remove(player.uniqueId)
    }

    private fun getToolMapName(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null
        val key = NamespacedKey(plugin, "tool_map_name")
        return item.itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING)
    }

    fun clearAll() {
        for (entities in activeDisplays.values) {
            for (entity in entities) {
                if (entity.isValid) entity.remove()
            }
        }
        activeDisplays.clear()
        currentTrackingMaterial.clear()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        clearPlayerDisplays(event.player)
    }

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        clearPlayerDisplays(event.player)
    }
}