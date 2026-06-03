package com.eastcompany.eastsub.jinro.task

import com.eastcompany.eastsub.jinro.Jinro
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

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
                    stringToLoc(mapData.lobby)?.let { requiredElements.add(Triple(it, Material.WHITE_WOOL, true)) }
                }
                Material.BLAZE_ROD -> {
                    mapData.spawns.forEach { str -> stringToLoc(str)?.let { requiredElements.add(Triple(it, Material.ORANGE_WOOL, true)) } }
                }
                Material.EMERALD -> {
                    mapData.shops.forEach { str -> stringToLoc(str)?.let { requiredElements.add(Triple(it, Material.EMERALD_BLOCK, true)) } }
                }
                Material.IRON_SWORD -> {
                    mapData.court?.let { str -> stringToLoc(str)?.let { requiredElements.add(Triple(it, Material.IRON_BLOCK, true)) } }
                }
                Material.IRON_PICKAXE -> {
                    // 鉄ピッケル（復活資源）の時は、サバイバルでも見えるGLASS（またはRED_STAINED_GLASS等）にするのがお勧め
                    mapData.resourceLocations.forEach { str -> stringToLoc(str)?.let { requiredElements.add(Triple(it, Material.GLASS, false)) } }
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
                    // === 本来の ArmorStand の生成処理 ===
                    // 座標の微調整（頭の位置にブロックを合わせるための Y+0.0 か Y-1.45 は環境に合わせて調整してください）
                    val spawnLoc = Location(loc.world, loc.blockX + 0.5, loc.blockY.toDouble(), loc.blockZ + 0.5 , loc.yaw, loc.pitch)

                    loc.world.spawn(spawnLoc, ArmorStand::class.java) { stand ->
                        stand.setGravity(false)
                        stand.isMarker = true
                        stand.isGlowing = true
                        stand.equipment.helmet = ItemStack(material)
                        stand.equipment.setItemInMainHand(ItemStack(material))
                        stand.setVisibleByDefault(false)
                        player.showEntity(plugin, stand)
                    }
                } else {
                    // === 本来の BlockDisplay の生成処理（鉄ピッケル専用など） ===
                    val spawnLoc = Location(loc.world, loc.blockX.toDouble(), loc.blockY.toDouble(), loc.blockZ.toDouble())

                    loc.world.spawn(spawnLoc, BlockDisplay::class.java) { blockDisplay ->
                        blockDisplay.block = Bukkit.createBlockData(material)
                        blockDisplay.isGlowing = true
                        blockDisplay.glowColorOverride = Color.fromRGB(0, 255, 0) // 緑色発光

                        // サイズをちょっと大きくする(1.1倍)
                        val transformation = blockDisplay.transformation
                        transformation.scale.set(1.1f, 1.1f, 1.1f)

                        // 中心がズレるのを防ぐための微調整
                        transformation.translation.set(-0.055f, -0.055f, -0.055f)

                        blockDisplay.transformation = transformation
                        blockDisplay.setVisibleByDefault(false)
                        player.showEntity(plugin, blockDisplay)
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

    private fun stringToLoc(str: String?): Location? {
        if (str == null) return null
        val parts = str.split(",")
        if (parts.size < 6) return null
        val world = Bukkit.getWorld(parts[0]) ?: return null
        return Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
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