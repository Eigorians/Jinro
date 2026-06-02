package com.eastcompany.eastsub.jinro.game

/*
class GameEndChecker(private val gameManager: GameManager) {

    /**
     * ゲーム終了をチェックし、勝利した陣営のリストを返す。
     * @return 勝利陣営のリスト（ゲーム続行の場合は空のリストを返す）
     */
    fun checkGameEnd(): List<Camp> {
        val allPlayers = gameManager.getAllPlayers()
        val alivePlayers = allPlayers.filter { it.isAlive }

        // 生存者が誰もいない場合はゲーム続行または引き分け（空リスト）
        if (alivePlayers.isEmpty()) return emptyList()

        // == 1. ゲーム終了判定 ==
        // 死神の単独生存チェック
        val isGrimReaperAlone = alivePlayers.all { it.camp == Camp.GRIM_REAPER }

        // 各主要陣営の全滅チェック（狂人は生存していても通常の終了トリガーにはならないため除外）
        val isVillagerAllDead = alivePlayers.none { it.camp == Camp.VILLAGER }
        val isWerewolfAllDead = alivePlayers.none { it.camp == Camp.WEREWOLF }
        val isGrimReaperAllDead = alivePlayers.none { it.camp == Camp.GRIM_REAPER }

        // 終了条件のいずれかを満たしているか
        val isGameOver = isGrimReaperAlone ||
                (isVillagerAllDead && isGrimReaperAllDead) ||
                (isWerewolfAllDead && isGrimReaperAllDead)

        // どの終了条件も満たしていない場合は、ゲームを続行する（空リストを返す）
        if (!isGameOver) return emptyList()


        // == 2. 勝利陣営の決定（優先順位順に判定し、Listで返す） ==

        // 各種フラグの用意
        val isFoxAlive = alivePlayers.any { it.role == Role.FOX }
        val totalLoversAssigned = allPlayers.count { it.isLovers }
        val aliveLoversCount = alivePlayers.count { it.isLovers }
        val isLoversAlive = totalLoversAssigned >= 2 && aliveLoversCount >= 2

        // 【例外用】生き残っている狐の中に、恋人が含まれているか
        val isFoxLoversAlive = alivePlayers.any { it.role == Role.FOX && it.isLovers }

        // --- 最終的な勝利陣営を入れる動的リスト ---
        val winnerCamps = mutableListOf<Camp>()

        // 【優先度：第1位】恋人陣営の勝利
        if (isLoversAlive && (!isFoxAlive || isFoxLoversAlive)) {
            winnerCamps.add(Camp.LOVERS)
            return winnerCamps
        }

        // 【優先度：第2位】狐陣営の勝利
        if (isFoxAlive) {
            winnerCamps.add(Camp.FOX)
            return winnerCamps
        }

        // 【優先度：第3位】通常の陣営勝利
        when {
            // ① 死神のみが生存
            isGrimReaperAlone -> {
                winnerCamps.add(Camp.GRIM_REAPER)
            }

            // ② 村人と死神が全滅 -> 人狼の勝利（＋狂人陣営も勝利！⚠️）
            isVillagerAllDead && isGrimReaperAllDead -> {
                winnerCamps.add(Camp.WEREWOLF)
                winnerCamps.add(Camp.MADMAN) // 人狼勝利時に狂人陣営を自動追加
            }

            // ③ 人狼と死神が全滅 -> 村人の勝利
            isWerewolfAllDead && isGrimReaperAllDead -> {
                winnerCamps.add(Camp.VILLAGER)
            }
        }

        return winnerCamps
    }
}
 */