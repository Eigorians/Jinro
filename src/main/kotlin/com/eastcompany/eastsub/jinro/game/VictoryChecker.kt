package com.eastcompany.eastsub.jinro.game

import com.eastcompany.eastsub.jinro.manager.JinroGameManager

class GameEndChecker(private val gameManager: JinroGameManager) {

    /**
     * ゲーム終了をチェックし、勝利した陣営のリストを返す。
     * @return 勝利陣営のリスト（ゲーム続行の場合は空のリストを返す）
     */
    fun checkGameEnd(): List<Camp> {
        // 💡 ✨ 修正: gamePlayers() から gamePlayers.values に変更して、登録データを取得
        val allPlayers = gameManager.gamePlayers.values
        val alivePlayers = allPlayers.filter { it.isAlive }

        // 生存者が誰もいない場合はゲーム続行または引き分け（空リスト）
        if (alivePlayers.isEmpty()) return emptyList()

        // == 1. ゲーム終了判定 ==
        val isGrimReaperAlone = alivePlayers.all { it.camp == Camp.GRIM_REAPER }

        val isVillagerAllDead = alivePlayers.none { it.camp == Camp.VILLAGER }
        val isWerewolfAllDead = alivePlayers.none { it.camp == Camp.WEREWOLF }
        val isGrimReaperAllDead = alivePlayers.none { it.camp == Camp.GRIM_REAPER }

        val isGameOver = isGrimReaperAlone ||
                (isVillagerAllDead && isGrimReaperAllDead) ||
                (isWerewolfAllDead && isGrimReaperAllDead)

        if (!isGameOver) return emptyList()

        // == 2. 勝利陣営の決定 ==
        val isFoxAlive = alivePlayers.any { it.role == Role.FOX }
        val totalLoversAssigned = allPlayers.count { it.isLovers }
        val aliveLoversCount = alivePlayers.count { it.isLovers }
        val isLoversAlive = totalLoversAssigned >= 2 && aliveLoversCount >= 2
        val isFoxLoversAlive = alivePlayers.any { it.role == Role.FOX && it.isLovers }

        val winnerCamps = mutableListOf<Camp>()

        if (isLoversAlive && (!isFoxAlive || isFoxLoversAlive)) {
            winnerCamps.add(Camp.LOVERS)
            return winnerCamps
        }

        if (isFoxAlive) {
            winnerCamps.add(Camp.FOX)
            return winnerCamps
        }

        when {
            isGrimReaperAlone -> {
                winnerCamps.add(Camp.GRIM_REAPER)
            }
            isVillagerAllDead -> {
                winnerCamps.add(Camp.WEREWOLF)
                winnerCamps.add(Camp.MADMAN)
            }
            true -> {
                winnerCamps.add(Camp.VILLAGER)
            }
        }

        return winnerCamps
    }
}