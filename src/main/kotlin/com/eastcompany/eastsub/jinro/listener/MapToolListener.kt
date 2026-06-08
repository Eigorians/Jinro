package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.config.MapData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
import kotlin.math.roundToInt

class MapToolListener(private val plugin: Jinro) : Listener {

    private fun getToolMapName(item: ItemStack?): String? {
        val key = NamespacedKey(plugin, "tool_map_name")
        return item?.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING)
    }

    private fun saveMapData(mapName: String, data: MapData, player: Player) {
        val currentDataMap = plugin.configManager.gameConfig.mapData.toMutableMap()
        currentDataMap[mapName] = data
        plugin.configManager.gameConfig.mapData = currentDataMap
        plugin.configManager.save()
        plugin.toolParticleTask.forceRefresh(player)
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = event.item ?: return
        val mapName = getToolMapName(item) ?: return
        val data = plugin.configManager.gameConfig.mapData[mapName] ?: MapData()

        val loc = player.location.block.location.clone().apply {
            x = blockX + 0.5
            z = blockZ + 0.5
            yaw = (player.yaw / 90.0F).roundToInt() * 90.0F
            pitch = 0f
        }

        // --- 右クリック処理（登録・追加） ---
        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true

            // 空中なら足元、ブロッククリックならそのブロックの座標を中心にスナップ
            val target = if (event.clickedBlock != null) {
                event.clickedBlock!!.location.clone().apply {
                    x = blockX + 0.5
                    z = blockZ + 0.5
                }
            } else loc

            when (item.type) {
                Material.WHITE_WOOL -> data.lobby = loc
                Material.BLAZE_ROD -> data.spawns += loc
                Material.EMERALD -> data.shops += loc
                Material.IRON_SWORD -> data.court = loc
                Material.BONE -> data.undergroundY = loc.blockY

                // 💡 各アイテムタイプで直接分岐
                Material.POPPY -> data.resourceFlowers += target
                Material.CHEST -> data.resourceChests += target
                Material.IRON_ORE -> data.resourceIrons += target
                Material.OAK_LOG -> data.resourceWoods += target
                else -> return
            }

            saveMapData(mapName, data, player)
            player.sendActionBar(Component.text("【$mapName】データを更新しました", NamedTextColor.GREEN))
        }

        // --- 左クリック処理（周囲の削除） ---
        if (event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            event.isCancelled = true

            // 資源系ツールは一律で判定距離を 3.0 マスにする
            val isResourceTool = item.type == Material.POPPY || item.type == Material.CHEST ||
                    item.type == Material.IRON_ORE || item.type == Material.OAK_LOG
            val limit = if (isResourceTool) 3.0 else 2.0

            when (item.type) {
                Material.BLAZE_ROD -> data.spawns = data.spawns.filter { it.distance(player.location) > limit }
                Material.EMERALD -> data.shops = data.shops.filter { it.distance(player.location) > limit }

                // 💡 各アイテムタイプで直接削除フィルターをかける
                Material.POPPY -> data.resourceFlowers = data.resourceFlowers.filter { it.distance(player.location) > limit }
                Material.CHEST -> data.resourceChests = data.resourceChests.filter { it.distance(player.location) > limit }
                Material.IRON_ORE -> data.resourceIrons = data.resourceIrons.filter { it.distance(player.location) > limit }
                Material.OAK_LOG -> data.resourceWoods = data.resourceWoods.filter { it.distance(player.location) > limit }
                else -> return
            }

            saveMapData(mapName, data, player)
            player.sendActionBar(Component.text("【$mapName】周囲のデータを削除しました", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (getToolMapName(event.player.inventory.itemInMainHand) != null) event.isCancelled = true
    }
}