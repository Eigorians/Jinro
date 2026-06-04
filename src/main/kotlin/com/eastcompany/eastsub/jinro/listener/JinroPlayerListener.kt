package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import com.eastcompany.eastsub.jinro.manager.JinroTimeManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent

class JinroPlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // 💡 現在ゲームのタイマー（BossBar）が動いている場合、再ログインしたプレイヤーをバーに追加する
        JinroTimeManager.getBossBar()?.let { bar ->
            bar.addPlayer(player)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        // ゲームが実行中でなければ、通常通りの死亡処理を行う
        if (!JinroGameManager.isGameRunning) return

        val player = event.entity
        val uuid = player.uniqueId

        // 1. 管理マップから死亡したプレイヤーのデータを取得
        val gamePlayer = JinroGameManager.gamePlayers[uuid] ?: return

        // 既に死亡している場合は処理をスキップ
        if (!gamePlayer.isAlive) return

        // 2. 内部ステータスを死亡（isAlive = false）に変更
        gamePlayer.isAlive = false

        // 3. 管理マップの参加者から削除し、観戦者セットへ移行
        JinroGameManager.activeSpectators.add(uuid)

        // 4. 即座にゲームモードをスペクテーター（SPECTATOR）に変更
        //    ※リスポーン画面を挟まず即座に観戦化させるため、リスポーン処理を調整
        player.gameMode = GameMode.SPECTATOR

        // 5. 死亡メッセージのカスタマイズ (ログの秘匿など必要に応じて)
        //    人狼ゲームでは「誰がどう死んだか」を隠す（あるいは独自の演出にする）ことが多いため、
        //    デフォルトの死亡ログを非表示にしてアナウンスを制御します。
        event.deathMessage(null)

        val deathNotice = Component.text()
            .append(Component.text("☠ ", NamedTextColor.RED))
            .append(Component.text(gamePlayer.name, NamedTextColor.WHITE, TextDecoration.BOLD))
            .append(Component.text(" が死亡しました。", NamedTextColor.GRAY))
            .build()

        // 全員に死亡通知を送信
        for (p in Bukkit.getOnlinePlayers()) {
            p.sendMessage(deathNotice)
        }

        // 💡 6. 死亡に伴うゲーム終了条件の判定
        // プレイヤーが一人死んだことで勝敗が決したかチェックします
        val endChecker = GameEndChecker(JinroGameManager)
        val winners = endChecker.checkGameEnd()

        if (winners.isNotEmpty()) {
            // 勝者が確定した場合の終了処理（※本来は勝者アナウンスなどのメソッドを呼ぶのが理想です）
            JinroGameManager.reset()

            val winNotice = Component.text("🎉 ゲームが終了しました！勝者陣営: ${winners.joinToString()}", NamedTextColor.GOLD, TextDecoration.BOLD)
            for (p in Bukkit.getOnlinePlayers()) {
                p.sendMessage(winNotice)
            }
        }
    }
}