package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin

object JinroTeleportManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    fun teleportPlayersToGamePositions() {
        val config = plugin.configManager.gameConfig
        val selectedMapName = config.selectedMap
        val mapData = config.mapData[selectedMapName] ?: return

        // ─── 🟡 1. 観戦者をロビー位置へテレポート ───
        val lobbyLoc = parseLocation(mapData.lobby ?: "")
        if (lobbyLoc != null) {
            // GameManager側で確定した観戦者リストをテレポート
            JinroGameManager.activeSpectators.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.teleport(lobbyLoc)
            }
        }

        // ─── 🟢 2. 参加者をスポーン位置へなるべくばらつくようにテレポート ───
        val spawnStrings = mapData.spawns
        if (spawnStrings.isEmpty()) return

        val spawnLocations = spawnStrings.mapNotNull { parseLocation(it) }.toMutableList()
        if (spawnLocations.isEmpty()) return

        // GameManagerが保持する本番参加者のPlayer実体を取得
        val activePlayers = JinroGameManager.gamePlayers.values.mapNotNull { gPlayer ->
            gPlayer.offlinePlayer.player
        }.toMutableList()

        if (activePlayers.isEmpty()) return

        // プレイヤーとスポーン座標をシャッフルしてばらつきを最大化
        activePlayers.shuffle()
        spawnLocations.shuffle()

        activePlayers.forEachIndexed { index, player ->
            val targetLocation = spawnLocations[index % spawnLocations.size]
            player.teleport(targetLocation)
            player.sendMessage(Component.text("🚨 スポーン地点に転送されました！", NamedTextColor.GOLD))
        }
    }

    private fun parseLocation(locStr: String): Location? {
        if (locStr.isBlank()) return null
        return runCatching {
            val parts = locStr.split(",")
            val world = Bukkit.getWorld(parts[0]) ?: return null
            Location(
                world,
                parts[1].toDouble(),
                parts[2].toDouble(),
                parts[3].toDouble(),
                parts.getOrNull(4)?.toFloat() ?: 0f,
                parts.getOrNull(5)?.toFloat() ?: 0f
            )
        }.getOrNull()
    }
}