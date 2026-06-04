package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.config.MapData
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

object JinroMatchManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)
    private var countdownTask: BukkitRunnable? = null
    private var activeBossBar: BossBar? = null

    // announce.json を指定するフォントキー
    private val announceFont = Key.key("minecraft:announce")

    var hostUniqueId: UUID? = null
    val participants = mutableSetOf<UUID>()
    val spectators = mutableSetOf<UUID>()
    val nonParticipants = mutableSetOf<UUID>()

    var isRecruiting = false

    fun getTotalRelevantPlayers(): Int {
        val onlineIds = Bukkit.getOnlinePlayers().map { it.uniqueId }.toSet()
        val activePlayers = onlineIds - nonParticipants
        return activePlayers.size
    }

    /**
     * 15秒のカウントダウンを開始する（事前の安全チェックとロビーテレポート含む）
     */
    fun startCountdown() {
        val configManager = plugin.configManager
        val config = configManager.gameConfig
        val selectedMapName = config.selectedMap
        val mapData = config.mapData[selectedMapName]

        // ─── 🛡️ マップロケーションの完全性チェック ───
        if (mapData == null || !isMapDataValid(mapData)) {
            val host = hostUniqueId?.let { Bukkit.getPlayer(it) }
            val errorMsg = Component.text("❌ マップ「$selectedMapName」の設定（ロビー、スポーン、ショップ、裁判所）が不完全なため、ゲームを開始できません。", NamedTextColor.RED)

            host?.sendMessage(errorMsg) ?: Bukkit.broadcast(errorMsg)
            return
        }

        // 募集受付を終了、タスク初期化
        isRecruiting = false
        countdownTask?.cancel()
        clearBossBar()

        // ─── 🚀 プレイヤーをロビーへ即座にテレポート ───
        val lobbyLocation = parseLocation(mapData.lobby!!)
        if (lobbyLocation != null) {
            (participants + spectators).forEach { uuid ->
                Bukkit.getPlayer(uuid)?.teleport(lobbyLocation)
            }
        }

        // 15秒前のアナウンス
        broadcastMessage(Component.text("まもなくゲームが開始されます！ (残り 15 秒)", NamedTextColor.GREEN))

        // ボスバーを作成して表示
        val initialBarTitle = Component.text("ゲーム開始まであと ", NamedTextColor.YELLOW)
            .append(Component.text("15", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.text(" 秒", NamedTextColor.YELLOW))

        val bossBar = BossBar.bossBar(initialBarTitle, 1.0f, BossBar.Color.RED, BossBar.Overlay.PROGRESS)
        activeBossBar = bossBar

        for (player in Bukkit.getOnlinePlayers()) {
            if (!nonParticipants.contains(player.uniqueId)) {
                player.showBossBar(bossBar)
            }
        }

        // ─── ⏳ 15秒カウントダウンタスク開始 ───
        countdownTask = object : BukkitRunnable() {
            var timeLeft = 15
            val maxTime = 15.0f

            override fun run() {
                timeLeft--

                // ボスバーの同期更新
                activeBossBar?.let { bar ->
                    val progress = (timeLeft.toFloat() / maxTime).coerceIn(0.0f, 1.0f)
                    bar.progress(progress)
                    bar.name(
                        Component.text("ゲーム開始まであと ", NamedTextColor.YELLOW)
                            .append(Component.text(timeLeft, NamedTextColor.RED, TextDecoration.BOLD))
                            .append(Component.text(" 秒", NamedTextColor.YELLOW))
                    )
                }

                // 残り3秒から大画面フォントを画面中央に叩き込む！
                when (timeLeft) {
                    3 -> {
                        sendCountdownTitle("\uE009")
                        playCountdownSound()
                    }
                    2 -> {
                        sendCountdownTitle("\uE008")
                        playCountdownSound()
                    }
                    1 -> {
                        sendCountdownTitle("\uE007")
                        playCountdownSound()
                    }
                }

                // 0秒になったら終了、GameManagerを起動！
                if (timeLeft <= 0) {
                    cancel()
                    countdownTask = null
                    clearBossBar()

                    // ✨ 💡 ここでJinroGameManagerへ参加者・観戦者リストを渡してバトンタッチ！
                    JinroGameManager.startGame(participants, spectators)
                }
            }
        }
        countdownTask?.runTaskTimer(plugin, 20L, 20L)
    }

    private fun isMapDataValid(data: MapData): Boolean {
        return !data.lobby.isNullOrBlank() &&
                data.spawns.isNotEmpty() &&
                data.shops.isNotEmpty() &&
                !data.court.isNullOrBlank()
    }

    private fun parseLocation(locStr: String): Location? {
        return runCatching {
            val parts = locStr.split(",")
            val world = Bukkit.getWorld(parts[0]) ?: return null
            Location(
                world,
                parts[1].toDouble(),
                parts[2].toDouble(),
                parts[3].toDouble(),
                parts.getOrNull(4)?.toFloat() ?: 0f,
                parts.getOrNull(5)?.toFloat() ?: 0f
            )
        }.getOrNull()
    }

    private fun sendCountdownTitle(character: String) {
        val titleComponent = Component.text(character).font(announceFont)
        val title = Title.title(titleComponent, Component.empty())

        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId
            if (!nonParticipants.contains(uuid)) {
                player.showTitle(title)
            }
        }
    }

    private fun clearBossBar() {
        activeBossBar?.let { bar ->
            for (player in Bukkit.getOnlinePlayers()) {
                player.hideBossBar(bar)
            }
        }
        activeBossBar = null
    }

    fun reset() {
        countdownTask?.cancel()
        countdownTask = null
        clearBossBar()
        hostUniqueId = null
        participants.clear()
        spectators.clear()
        nonParticipants.clear()
        isRecruiting = false
    }

    private fun playCountdownSound() {
        for (player in Bukkit.getOnlinePlayers()) {
            if (!nonParticipants.contains(player.uniqueId)) {
                // UI_BUTTON_CLICK を 音量1.0, ピッチ1.0 で再生（お好みで BLOCK_NOTE_BLOCK_HAT などに変更も可）
                player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
            }
        }
    }

    private fun broadcastMessage(component: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(component)
        }
    }
}