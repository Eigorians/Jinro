package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

object JinroTeleportManager {
    private val plugin = Jinro.instance

    fun teleportPlayersToGamePositions() {
        val config = plugin.configManager.gameConfig
        val selectedMapName = config.selectedMap
        val mapData = config.mapData[selectedMapName] ?: return

        // ─── 🟡 1. 観戦者をロビー位置へテレポート ───
        val lobbyLoc = mapData.lobby
        if (lobbyLoc != null) {
            // GameManager側で確定した観戦者リストをテレポート
            JinroGameManager.activeSpectators.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.teleport(lobbyLoc)
            }
        }

        // ─── 🟢 2. 参加者をスポーン位置へなるべくばらつくようにテレポート ───
        val spawnStrings = mapData.spawns
        if (spawnStrings.isEmpty()) return

        val spawnLocations = mapData.spawns.toMutableList()
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
        }
    }

}