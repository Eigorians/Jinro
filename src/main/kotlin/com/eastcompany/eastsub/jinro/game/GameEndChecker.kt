package com.eastcompany.eastsub.jinro.game

import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import com.eastcompany.eastsub.jinro.manager.JinroGameEndManager

class GameEndChecker(private val gameManager: JinroGameManager) {

    /**
     * ゲーム終了をチェックし、終了条件を満たしていれば終了処理を実行する（毎フェーズ用）
     */
    fun checkGameEnd() {
        val allPlayers = gameManager.gamePlayers.values
        val alivePlayers = allPlayers.filter { it.isAlive }

        if (alivePlayers.isEmpty()) {
            JinroGameEndManager.endGame(null)
            return
        }

        if (alivePlayers.all { it.camp == Camp.LOVERS }) {
            JinroGameEndManager.endGame(Camp.LOVERS)
            return
        }

        val isOnlyFoxCampAlive = alivePlayers.all { it.camp == Camp.FOX }
        val isRealFoxDead = alivePlayers.none { it.role == Role.FOX }
        if (isOnlyFoxCampAlive && isRealFoxDead) {
            JinroGameEndManager.endGame(null)
            return
        }

        val isGrimReaperAlone = alivePlayers.all { it.camp == Camp.GRIM_REAPER }
        val isVillagerAllDead = alivePlayers.none { it.camp == Camp.VILLAGER }
        val isJINROAllDead = alivePlayers.none { it.camp == Camp.JINRO }
        val isGrimReaperAllDead = alivePlayers.none { it.camp == Camp.GRIM_REAPER }

        val isGameOver = isGrimReaperAlone ||
                (isVillagerAllDead && isGrimReaperAllDead) ||
                (isJINROAllDead && isGrimReaperAllDead)

        if (!isGameOver) return

        val isFoxAlive = alivePlayers.any { it.role == Role.FOX }
        val totalLoversAssigned = allPlayers.count { it.isLovers }
        val aliveLoversCount = alivePlayers.count { it.isLovers }
        val isLoversAlive = totalLoversAssigned >= 2 && aliveLoversCount >= 2

        if (isLoversAlive) {
            JinroGameEndManager.endGame(Camp.LOVERS)
            return
        }

        if (isFoxAlive) {
            JinroGameEndManager.endGame(Camp.FOX)
            return
        }

        when {
            isGrimReaperAlone -> JinroGameEndManager.endGame(Camp.GRIM_REAPER)
            isVillagerAllDead -> JinroGameEndManager.endGame(Camp.JINRO)
            else -> JinroGameEndManager.endGame(Camp.VILLAGER)
        }
    }

    /**
     * 💡 【新規追加】ゲーム開始時に、すでに終了条件を満たしてしまっているか検証する
     * @return 終了条件を満たしている（設定が異常）なら true
     */
    fun isInitialStateInvalid(): Boolean {
        val alivePlayers = gameManager.gamePlayers.values.filter { it.isAlive }

        // 参加者がいない、または役職配布の時点で生存者がいない場合
        if (alivePlayers.isEmpty()) return true

        // 全員が最初から恋人、または背徳者(狐なし)しかいない特異ケース
        if (alivePlayers.all { it.camp == Camp.LOVERS }) return true
        if (alivePlayers.all { it.camp == Camp.FOX } && alivePlayers.none { it.role == Role.FOX }) return true

        // 死神が単独生存、または村人全滅、人狼全滅の初期構成
        val isGrimReaperAlone = alivePlayers.all { it.camp == Camp.GRIM_REAPER }
        val isVillagerAllDead = alivePlayers.none { it.camp == Camp.VILLAGER }
        val isJINROAllDead = alivePlayers.none { it.camp == Camp.JINRO }
        val isGrimReaperAllDead = alivePlayers.none { it.camp == Camp.GRIM_REAPER }

        return isGrimReaperAlone ||
                (isVillagerAllDead && isGrimReaperAllDead) ||
                (isJINROAllDead && isGrimReaperAllDead)
    }
}