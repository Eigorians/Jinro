package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.RoleBookManager
import com.eastcompany.eastsub.jinro.listener.JinroPlayerListener // 💡 追加
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList // 💡 追加
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.UUID

object JinroGameManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    private val role_view = Key.key("minecraft:role_view")

    // ─── 💡 リスナーのインスタンスを保持する変数 ───
    private var gameListener: JinroPlayerListener? = null

    val gamePlayers = mutableMapOf<UUID, GamePlayer>()
    val activeSpectators = mutableSetOf<UUID>()
    var isGameRunning = false

    fun startGame(finalParticipants: Set<UUID>, finalSpectators: Set<UUID>) {
        isGameRunning = true
        gamePlayers.clear()
        activeSpectators.clear()

        // ─── 💡 1. ゲーム開始時にリスナーを動的に登録 ───
        if (gameListener == null) {
            gameListener = JinroPlayerListener()
            Bukkit.getPluginManager().registerEvents(gameListener!!, plugin)
        }

        // 1. 参加者のデータをオンライン判定の上、GamePlayerマップにコンバート＆保持
        finalParticipants.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                val gamePlayer = GamePlayer(offlinePlayer = player, role = Role.VILLAGER)
                gamePlayer.resetState()
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

        // 開始直前の終了条件チェック
        val endChecker = GameEndChecker(this)
        val initialWinners = endChecker.checkGameEnd()

        if (initialWinners.isNotEmpty()) {
            // エラー終了時も正しく初期化（リスナー解除を含む）するために reset() を呼ぶ
            reset()

            broadcastMessage(
                Component.text("❌ 【ゲーム開始エラー】設定された役職のバランス、または参加人数が原因で、開始時点で終了条件を満たしているため強制終了しました。設定を見直してください。", NamedTextColor.RED, TextDecoration.BOLD)
            )
            return
        }

        // 4. 分散テレポートクラスを呼び出し
        JinroTeleportManager.teleportPlayersToGamePositions()

        // 全参加プレイヤーに役職図鑑を配布する
        giveRoleBookToPlayers()

        // 5. リソースパックを100%活かした全画面の役職決定演出を叩き込む
        sendRoleAssignmentTitles()

        // 6. 全画面一斉ゲーム開始アナウンス
        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))
        broadcastMessage(Component.text("       人狼ゲームが開始されました！       ", NamedTextColor.RED, TextDecoration.BOLD))
        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))

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

        object : BukkitRunnable() {
            override fun run() {
                if (!isGameRunning) return
                JinroTimeManager.startTimer()
            }
        }.runTaskLater(plugin, 100L)
    }

    private fun giveRoleBookToPlayers() {
        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach
            val personalRoleBook = RoleBookManager.createRoleBook(gPlayer.role)
            player.inventory.addItem(personalRoleBook)
        }
    }

    private fun sendRoleAssignmentTitles() {
        val times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(4000),
            Duration.ofMillis(500)
        )

        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach

            val mainTitleComponent = Component.text("\uE000").font(role_view)
            val subTitleComponent = Component.text(gPlayer.role.role_view).font(role_view)

            val combinedTitle = Title.title(mainTitleComponent, subTitleComponent, times)
            player.showTitle(combinedTitle)

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

        // ─── 💡 2. ゲーム終了（リセット）時にリスナーを解除 ───
        gameListener?.let { listener ->
            HandlerList.unregisterAll(listener) // このリスナーに紐づく全てのイベントを解除
            gameListener = null // 参照をクリア
        }

        JinroTimeManager.stopTimer()
    }

    private fun broadcastMessage(component: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(component)
        }
    }
}