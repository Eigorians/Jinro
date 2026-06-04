package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.config.MapData
import com.eastcompany.eastsub.jinro.util.LocationUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MapToolListener(private val plugin: Jinro) : Listener {

    private fun getToolMapName(item: ItemStack?): String? {
        if (item == null || !item.hasItemMeta()) return null
        val key = NamespacedKey(plugin, "tool_map_name")
        return item.itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING)
    }

    private fun locToString(loc: Location): String {
        return "${loc.world.name},${loc.x},${loc.y},${loc.z},${loc.yaw},${loc.pitch}"
    }

    private fun stringToLoc(str: String): Location? {
        val parts = str.split(",")
        if (parts.size < 6) return null
        val world = org.bukkit.Bukkit.getWorld(parts[0]) ?: return null
        return Location(world, parts[1].toDouble(), parts[2].toDouble(), parts[3].toDouble(), parts[4].toFloat(), parts[5].toFloat())
    }

    private fun getOrCreateMapData(mapName: String): MapData {
        val currentDataMap = plugin.configManager.gameConfig.mapData.toMutableMap()
        return currentDataMap[mapName] ?: MapData()
    }

    // マップデータを最新に更新してファイルセーブし、プレイヤーの視点タスクを強制リフレッシュする
    private fun saveMapData(mapName: String, data: MapData, player: Player) {
        val currentDataMap = plugin.configManager.gameConfig.mapData.toMutableMap()
        currentDataMap[mapName] = data
        plugin.configManager.gameConfig.mapData = currentDataMap
        plugin.configManager.save()

        // タスク側へ通知して、即座にブロック表示を更新させる
        plugin.toolParticleTask.forceRefresh(player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        val mapName = getToolMapName(item) ?: return

        // === 右クリック時の登録処理 ===
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true
            val data = getOrCreateMapData(mapName)

            // 基本的なプレイヤーの足元座標（クリーン化）
            val playerLoc = LocationUtil.cleanLocation(player.location)
            val playerStr = locToString(playerLoc)

            when (item.type) {
                Material.WHITE_WOOL -> {
                    data.lobby = playerStr
                    saveMapData(mapName, data, player)
                    player.sendActionBar(Component.text("【$mapName】ロビー位置を登録しました", NamedTextColor.GREEN))
                }
                Material.BLAZE_ROD -> {
                    val list = data.spawns.toMutableList()
                    list.add(playerStr)
                    data.spawns = list
                    saveMapData(mapName, data, player)
                    player.sendActionBar(Component.text("【$mapName】スポーン位置を追加しました (${list.size}箇所目)", NamedTextColor.GREEN))
                }
                Material.EMERALD -> {
                    val list = data.shops.toMutableList()
                    list.add(playerStr)
                    data.shops = list
                    saveMapData(mapName, data, player)
                    player.sendActionBar(Component.text("【$mapName】ショップ位置を追加しました (${list.size}箇所目)", NamedTextColor.GREEN))
                }
                Material.IRON_SWORD -> {
                    data.court = playerStr
                    saveMapData(mapName, data, player)
                    player.sendActionBar(Component.text("【$mapName】裁判所位置を登録しました", NamedTextColor.GREEN))
                }
                Material.BONE -> {
                    data.undergroundY = playerLoc.blockY
                    saveMapData(mapName, data, player)
                    player.sendActionBar(Component.text("【$mapName】地下の基準高さを Y:${playerLoc.blockY} に固定しました", NamedTextColor.GREEN))
                }
                Material.IRON_PICKAXE -> {
                    val targetStr: String
                    val message: String

                    if (event.action == Action.RIGHT_CLICK_BLOCK && event.clickedBlock != null) {
                        // ブロックを右クリック：ターゲットブロックの中心を設定
                        val blockLoc = event.clickedBlock!!.location.clone().add(0.5, 0.0, 0.5)
                        targetStr = locToString(blockLoc)
                        message = "復活資源（ブロック）を登録しました"
                    } else {
                        // 空気を右クリック：他と同じくプレイヤー地点を設定
                        targetStr = playerStr
                        message = "復活資源（プレイヤー地点）を登録しました"
                    }

                    val list = data.resourceLocations.toMutableList()
                    if (!list.contains(targetStr)) {
                        list.add(targetStr)
                        data.resourceLocations = list
                        saveMapData(mapName, data, player)
                        player.sendActionBar(Component.text("【$mapName】$message (${list.size}個目)", NamedTextColor.GREEN))
                    } else {
                        player.sendActionBar(Component.text("既に同じ座標が登録されています", NamedTextColor.YELLOW))
                    }
                }
                else -> {}
            }
        }

        // === 左クリック時の削除処理 ===
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            val targetType = item.type
            if (targetType == Material.BLAZE_ROD || targetType == Material.EMERALD || targetType == Material.IRON_PICKAXE) {
                event.isCancelled = true
                val data = getOrCreateMapData(mapName)

                if (event.action == Action.LEFT_CLICK_BLOCK && event.clickedBlock != null && targetType == Material.IRON_PICKAXE) {
                    // 【鉄ピッケル専用】ブロックを左クリック：その位置をピンポイントでデータ削除
                    val blockLoc = event.clickedBlock!!.location.clone().add(0.5, 0.0, 0.5)
                    val blockStr = locToString(blockLoc)

                    val list = data.resourceLocations.toMutableList()
                    if (list.remove(blockStr)) {
                        data.resourceLocations = list
                        saveMapData(mapName, data, player)
                        player.sendActionBar(Component.text("【$mapName】指定ブロックの復活資源を削除しました", NamedTextColor.RED))
                    } else {
                        player.sendActionBar(Component.text("このブロックは登録されていません", NamedTextColor.GRAY))
                    }
                } else {
                    // 【共通】空気を左クリック（ピッケルは3マス、他は2マス以内の近接削除）
                    val currentList = when (targetType) {
                        Material.BLAZE_ROD -> data.spawns
                        Material.EMERALD -> data.shops
                        Material.IRON_PICKAXE -> data.resourceLocations
                        else -> return
                    }

                    val originalSize = currentList.size
                    val playerLoc = player.location
                    val limitDistance = if (targetType == Material.IRON_PICKAXE) 3.0 else 2.0

                    val filteredList = currentList.filter { str ->
                        val targetLoc = stringToLoc(str) ?: return@filter true
                        targetLoc.distance(playerLoc) > limitDistance
                    }

                    val deletedCount = originalSize - filteredList.size
                    if (deletedCount > 0) {
                        when (targetType) {
                            Material.BLAZE_ROD -> data.spawns = filteredList
                            Material.EMERALD -> data.shops = filteredList
                            Material.IRON_PICKAXE -> data.resourceLocations = filteredList
                            else -> {}
                        }
                        saveMapData(mapName, data, player)
                        player.sendActionBar(Component.text("【$mapName】近接${limitDistance.toInt()}マス以内のデータを $deletedCount 個削除しました", NamedTextColor.RED))
                    } else {
                        player.sendActionBar(Component.text("周囲${limitDistance.toInt()}マス以内に登録データがありません", NamedTextColor.GRAY))
                    }
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        // マップ設定ツール全般を持っている時は、誤操作でのブロック破壊をガードする
        if (getToolMapName(item) != null) {
            event.isCancelled = true
        }
    }
}