package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.RoleBookManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.UUID

object JinroGameManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    private val role_view = Key.key("minecraft:role_view")

    // ─── 🎮 オンライン中かつ参加中のGamePlayerデータを責任持って保持するマップ ───
    val gamePlayers = mutableMapOf<UUID, GamePlayer>()

    // 観戦中かつオンラインのプレイヤーUUIDを保持するセット
    val activeSpectators = mutableSetOf<UUID>()

    var isGameRunning = false

    /**
     * MatchManagerの15秒カウントダウン終了時に呼び出される、ゲーム本編の起動メソッド
     */
    fun startGame(finalParticipants: Set<UUID>, finalSpectators: Set<UUID>) {
        isGameRunning = true
        gamePlayers.clear()
        activeSpectators.clear()

        // 1. 参加者のデータをオンライン判定の上、GamePlayerマップにコンバート＆保持
        finalParticipants.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                val gamePlayer = GamePlayer(offlinePlayer = player, role = Role.VILLAGER)
                gamePlayer.resetState() // インベントリクリア、体力満腹度最大、アドベンチャーモード化
                gamePlayers[uuid] = gamePlayer
            }
        }

        // 2. 観戦者のデータをオンライン判定の上、保持
        finalSpectators.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                activeSpectators.add(uuid)
            }
        }

        // 3. 役職をプレイヤーたちへ抽選・配布する
        JinroRoleManager.distributeRoles(gamePlayers)

        // ─── 🛡️ 開始直前の終了条件チェック（誤爆・事故防止） ───
        val endChecker = GameEndChecker(this)
        val initialWinners = endChecker.checkGameEnd()

        if (initialWinners.isNotEmpty()) {
            isGameRunning = false
            gamePlayers.clear()
            activeSpectators.clear()

            broadcastMessage(
                Component.text("❌ 【ゲーム開始エラー】設定された役職のバランス、または参加人数が原因で、開始時点で終了条件を満たしているため強制終了しました。設定を見直してください。", NamedTextColor.RED, TextDecoration.BOLD)
            )
            return
        }

        // ─── 🚀 ここから本番のゲーム開始シーケンス ───

        // 4. 🚨 分散テレポートクラスを呼び出し
        JinroTeleportManager.teleportPlayersToGamePositions()

        // 📖 全参加プレイヤーに役職図鑑を配布する
        giveRoleBookToPlayers()

        // 5. リソースパックを100%活かした全画面の役職決定演出を叩き込む
        sendRoleAssignmentTitles()

        // 6. 全画面一斉ゲーム開始アナウンス
        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))
        broadcastMessage(Component.text("       人狼ゲームが開始されました！       ", NamedTextColor.RED, TextDecoration.BOLD))
        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))

        // 各自への役割通知
        gamePlayers.values.forEach { gPlayer ->
            gPlayer.offlinePlayer.player?.sendMessage(
                Component.text("あなたは「プレイヤー」としてゲームに参加します。周りを警戒してください。", NamedTextColor.GREEN)
            )
        }

        activeSpectators.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.sendMessage(
                Component.text("あなたは「観戦者」としてゲームを視聴します。", NamedTextColor.YELLOW)
            )
        }

        // 💡 5秒間の役職確認演出（Title）が終わるタイミングを見計らって本編タイマーを起動
        object : BukkitRunnable() {
            override fun run() {
                if (!isGameRunning) return

                // 💡 ✨【変更】時間管理クラス（JinroTimeManager）を呼び出し、1日目の朝のBossBarとカウントダウンをスタート
                JinroTimeManager.startTimer()
            }
        }.runTaskLater(plugin, 100L) // 5秒後 (100 ticks) に実行
    }

    /**
     * 💡 参加プレイヤー全員のインベントリに役職図鑑を配布
     */
    private fun giveRoleBookToPlayers() {
        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach

            val personalRoleBook = RoleBookManager.createRoleBook(gPlayer.role)

            player.inventory.addItem(personalRoleBook)
        }
    }

    /**
     * 設定された role_view のデカ文字ロゴをタイトル表示し、
     * さらに sounds.json で定義された陣営専用のカスタムサウンドを再生する
     */
    private fun sendRoleAssignmentTitles() {
        val times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(4000),
            Duration.ofMillis(500)
        )

        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach

            // ─── 1. タイトル表示処理 ───
            val mainTitleComponent = Component.text("\uE000").font(role_view)
            val subTitleComponent = Component.text(gPlayer.role.role_view).font(role_view)

            val combinedTitle = Title.title(mainTitleComponent, subTitleComponent, times)
            player.showTitle(combinedTitle)

            // ─── 2. 陣営サウンド再生処理 (sounds.json依存) ───
            val camp = gPlayer.role.camp
            val soundKey = net.kyori.adventure.key.Key.key(camp.soundName)

            val campSound = net.kyori.adventure.sound.Sound.sound(
                soundKey,
                net.kyori.adventure.sound.Sound.Source.MASTER,
                1.0f,
                1.0f
            )

            player.playSound(campSound)
        }
    }

    /**
     * ゲーム終了時、または強制停止時の完全初期化リセット
     */
    fun reset() {
        gamePlayers.clear()
        activeSpectators.clear()
        isGameRunning = false

        // 💡 ✨【追加】ゲーム停止時にBossBarや進行中のタイマータスクも完全に破棄・クリアする
        JinroTimeManager.stopTimer()
    }

    private fun broadcastMessage(component: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(component)
        }
    }
}