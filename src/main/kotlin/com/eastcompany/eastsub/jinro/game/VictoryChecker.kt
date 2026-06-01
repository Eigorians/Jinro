package com.eastcompany.eastsub.jinro.game

class GameEndChecker(private val gameManager: GameManager) {

    fun checkGameEnd(): Camp? {
        val allPlayers = gameManager.getAllPlayers()
        val alivePlayers = allPlayers.filter { it.isAlive }

        // 生存者が誰もいない場合はゲーム終了（引き分け、または基本あり得ないパターン）
        if (alivePlayers.isEmpty()) return null

        // == 1. ゲーム終了判定 ==
        // 死神の単独生存チェック
        val isGrimReaperAlone = alivePlayers.all { it.camp == Camp.GRIM_REAPER }

        // 通常陣営の全滅チェック
        val isVillagerAllDead = alivePlayers.none { it.camp == Camp.VILLAGER }
        val isWerewolfAllDead = alivePlayers.none { it.camp == Camp.WEREWOLF }
        val isGrimReaperAllDead = alivePlayers.none { it.camp == Camp.GRIM_REAPER }

        // 終了条件のいずれかを満たしているか
        val isGameOver = isGrimReaperAlone ||
                (isVillagerAllDead && isGrimReaperAllDead) ||
                (isWerewolfAllDead && isGrimReaperAllDead)

        // どの終了条件も満たしていない場合は、ゲームを続行する
        if (!isGameOver) return null


        // == 2. 勝利陣営の決定（優先順位順に横取り判定） ==

        // 各種フラグの用意
        val isFoxAlive = alivePlayers.any { it.role == Role.FOX }
        val totalLoversAssigned = allPlayers.count { it.isLovers }
        val aliveLoversCount = alivePlayers.count { it.isLovers }
        val isLoversAlive = totalLoversAssigned >= 2 && aliveLoversCount >= 2

        // 【例外用】生き残っている狐の中に、恋人が含まれているか
        val isFoxLoversAlive = alivePlayers.any { it.role == Role.FOX && it.isLovers }

        // 【優先度：第1位】恋人陣営の勝利（例外対応含む）
        // 恋人が2人とも生存しており、かつ「狐が生き残っていない」または「生き残っている狐自身が恋人である」場合
        if (isLoversAlive && (!isFoxAlive || isFoxLoversAlive)) {
            return Camp.LOVERS
        }

        // 【優先度：第2位】狐陣営の勝利
        // 恋人が生存していない、あるいは「狐と（狐に関係ない別の）恋人が同時に生存してゲームが終わった」場合は狐の勝ち
        if (isFoxAlive) {
            return Camp.FOX
        }

        // 【優先度：第3位】通常の陣営勝利
        return when {
            // ① 死神のみが生存
            isGrimReaperAlone -> Camp.GRIM_REAPER

            // ② 村人と死神が全滅 -> 人狼の勝利
            isVillagerAllDead && isGrimReaperAllDead -> Camp.WEREWOLF

            // ③ 人狼と死神が全滅 -> 村人の勝利
            isWerewolfAllDead && isGrimReaperAllDead -> Camp.VILLAGER

            else -> null
        }
    }
}