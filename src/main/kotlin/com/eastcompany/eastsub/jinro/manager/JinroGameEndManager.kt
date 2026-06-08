package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration

object JinroGameEndManager {

    private val roleViewFont = Key.key("minecraft:role_view")
    private val resultview = Key.key("minecraft:result")

    /**
     * ゲームを正式に終了処理する
     * @param winnerCamp 勝利した陣営。引き分けの場合は null
     */
    fun endGame(winnerCamp: Camp?) {
        val resultChar = winnerCamp?.result ?: "Z"

        // ─── 🎬 演出1: 画面暗転用タイトルの構築 ───
        val blackScreenTimes = Title.Times.times(
            Duration.ofMillis(200),  // パッと暗転させるための短いフェードイン
            Duration.ofMillis(800),  // 少しの間、真っ黒に保つ
            Duration.ofMillis(500)   // 次の文字表示に向けてフェードアウト
        )
        // メインタイトルに暗転文字を設定
        val blackScreenTitle = Title.title(
            Component.text("\uE000").font(roleViewFont),
            Component.empty(),
            blackScreenTimes
        )

        // ─── 🎬 演出2: 勝敗結果（リザルトリソース）タイトルの構築 ───
        val resultTimes = Title.Times.times(
            Duration.ofMillis(500),  // 暗転から浮かび上がらせる
            Duration.ofMillis(5000), // 勝利画面をしっかり5秒間見せる
            Duration.ofMillis(1000)  // ゆっくり消えてロビーの景色に戻す
        )
        // 💡 変更点: resultChar（陣営画像）を「メインタイトル」に設定
        val victoryTitle = Title.title(
            Component.text(resultChar).font(resultview),
            Component.empty(),
            resultTimes
        )

        // ─── 🌍 ロビー位置の取得と安全策 ───
        val plugin = Jinro.instance
        val config = plugin.configManager.gameConfig
        val mapData = config.mapData[config.selectedMap]
        val lobbyLocation = mapData?.lobby

        if (lobbyLocation == null) {
            plugin.logger.warning("選択されたマップ [${config.selectedMap}] のロビー座標が設定されていません。")
        }

        // ─── 👥 全プレイヤーへの初期処理（暗転とテレポート） ───
        Bukkit.getOnlinePlayers().forEach { player ->
            // ① まず画面を真っ黒にする
            player.showTitle(blackScreenTitle)

            // ② 暗転中にロビーへテレポートして、移動の瞬間を見せないようにする
            if (lobbyLocation != null) {
                player.teleport(lobbyLocation)
            } else {
                player.teleport(player.world.spawnLocation)
            }

            // ③ プレイヤーのステータス完全リセット
            player.gameMode = GameMode.SURVIVAL
            player.health = 20.0
            player.foodLevel = 20
            player.activePotionEffects.forEach { effect -> player.removePotionEffect(effect.type) }
            player.inventory.clear()
        }

        // ─── ⏱️ 遅延タスク: 暗転が完了したタイミングでリザルト文字を表示 ───
        // 10 ticks = 0.5秒後。暗転文字がしっかり表示されたタイミングで上書きします
        object : BukkitRunnable() {
            override fun run() {
                Bukkit.getOnlinePlayers().forEach { player ->
                    player.showTitle(victoryTitle)
                }
            }
        }.runTaskLater(plugin, 10L)

        // ─── ⚙️ システム側のデータを一括リセット ───
        JinroMatchManager.reset()
        JinroGameManager.reset()

        // プレビューエンティティの全消去
        plugin.toolParticleTask.cancel()
        plugin.toolParticleTask.clearAll()

        // スコアボードの完全クリア
        JinroScoreboardManager.clearBoard()
    }
}