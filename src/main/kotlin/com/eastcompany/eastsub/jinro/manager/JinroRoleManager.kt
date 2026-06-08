package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import java.util.*

object JinroRoleManager {
    private val plugin: Jinro get() = Jinro.instance

    /**
     * 現在のゲーム参加者に対して、設定に基づいた役職を配布する
     */
    fun distributeRoles(gamePlayers: MutableMap<UUID, GamePlayer>) {
        val config = plugin.configManager.gameConfig
        val registeredRoles = config.registeredRoles
        val fixedWerewolfCount = config.fixedWerewolfCount

        // 1. 参加プレイヤーのリストを取得して完全にシャッフル
        val playerList = gamePlayers.values.toMutableList()
        playerList.shuffle()

        // 配布確定した役職を格納する暫定マップ (Player -> Role)
        val assignedRolesMap = mutableMapOf<GamePlayer, Role>()
        var remainingPlayers = playerList.toList()

        // ─── 🔺 A. 【fixedWerewolfCount >= 1】人数固定機能が有効な場合 ───
        if (fixedWerewolfCount > 0 && playerList.isNotEmpty()) {
            val werewolfTargets = playerList.take(fixedWerewolfCount)
            werewolfTargets.forEach { gamePlayer ->
                assignedRolesMap[gamePlayer] = Role.JINRO
            }
            remainingPlayers = playerList.drop(fixedWerewolfCount)
        }

        // ─── 🎲 B. 抽選箱（リスト）の作成 ───
        val roleLotteryBox = mutableListOf<Role>()
        registeredRoles.forEach { (role, count) ->
            if (fixedWerewolfCount > 0 && role == Role.JINRO) {
                return@forEach
            }
            repeat(count) {
                roleLotteryBox.add(role)
            }
        }

        // 💡 ✨【重要修正】
        // 箱全体を一度シャッフルし、そこから「残りのプレイヤー人数分」だけをランダムに型抜き（take）する。
        // その後、型抜きした役職リストをさらにシャッフルすることで、取り出された役職の並び順も完全にバラバラにします。
        roleLotteryBox.shuffle()
        val finalPickedRoles = roleLotteryBox.take(remainingPlayers.size).toMutableList()
        finalPickedRoles.shuffle()

        // ─── 🟢 C. 残りのプレイヤーへ選ばれた役職を上から順番に配布 ───
        remainingPlayers.forEachIndexed { index, gamePlayer ->
            // 💡 抽出したfinalPickedRolesから配る。もし足りない（プレイヤー数が設定枠を超えた）なら「村人」
            val assignedRole = finalPickedRoles.getOrNull(index) ?: Role.MURABITO
            assignedRolesMap[gamePlayer] = assignedRole
        }

        // ─── 💾 D. 最終決定した役職データをプレイヤーデータに一括反映 ───
        assignedRolesMap.forEach { (gamePlayer, role) ->
            gamePlayer.role = role
            gamePlayer.camp = role.camp
        }
    }
}