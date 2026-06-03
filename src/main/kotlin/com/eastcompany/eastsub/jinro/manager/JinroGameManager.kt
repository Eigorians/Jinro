package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.config.MapData
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

object JinroGameManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)
    private var countdownTask: BukkitRunnable? = null

    // announce.json を指定するフォントキー
    private val announceFont = Key.key("minecraft:announce")

    // 募集中の主催者（開始ボタンを押せる人）
    var hostUniqueId: UUID? = null

    // ─── 👥 プレイヤー状態の内部保持 ───
    private val rawParticipants = mutableSetOf<UUID>()
    private val rawSpectators = mutableSetOf<UUID>()
    private val rawNonParticipants = mutableSetOf<UUID>()

    // ─── 💡 オンライン中のプレイヤーのみをフィルタリングして取得するプロパティ ───
    val participants: Set<UUID>
        get() = rawParticipants.filter { Bukkit.getPlayer(it)?.isOnline == true }.toSet()

    val spectators: Set<UUID>
        get() = rawSpectators.filter { Bukkit.getPlayer(it)?.isOnline == true }.toSet()

    val nonParticipants: Set<UUID>
        get() = rawNonParticipants.filter { Bukkit.getPlayer(it)?.isOnline == true }.toSet()

    // 状態フラグ
    var isRecruiting = false
    var isGameRunning = false

    /**
     * 内部リストへの追加・削除用関数
     */
    fun addParticipant(uuid: UUID) = rawParticipants.add(uuid)
    fun addSpectator(uuid: UUID) = rawSpectators.add(uuid)
    fun addNonParticipant(uuid: UUID) = rawNonParticipants.add(uuid)

    fun removeFromAllLists(uuid: UUID) {
        rawParticipants.remove(uuid)
        rawSpectators.remove(uuid)
        rawNonParticipants.remove(uuid)
    }

    /**
     * 現在サーバーにいる「不参加」以外の有効なオンラインプレイヤーの総数を計算
     */
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

        // ─── 🛡️ 1. マップロケーションの完全性チェック ───
        if (mapData == null || !isMapDataValid(mapData)) {
            val host = hostUniqueId?.let { Bukkit.getPlayer(it) }
            val errorMsg = Component.text("❌ マップ「$selectedMapName」の設定（ロビー、スポーン、ショップ、裁判所）が不完全なため、ゲームを開始できません。", NamedTextColor.RED)

            host?.sendMessage(errorMsg) ?: Bukkit.broadcast(errorMsg)
            return
        }

        // 募集受付を終了
        isRecruiting = false
        countdownTask?.cancel()

        // ─── 🚀 2. オンライン中かつ参加・観戦のプレイヤーをロビーへ即座にテレポート ───
        val lobbyLocation = parseLocation(mapData.lobby!!)
        if (lobbyLocation != null) {
            // getterを介してオンライン中のプレイヤーのみにテレポートを実行
            (participants + spectators).forEach { uuid ->
                Bukkit.getPlayer(uuid)?.teleport(lobbyLocation)
            }
        }

        broadcastMessage(Component.text("まもなくゲームが開始されます！ (残り 15 秒)", NamedTextColor.GREEN))

        // ─── ⏳ 3. カウントダウンタスク開始 ───
        countdownTask = object : BukkitRunnable() {
            var timeLeft = 15

            override fun run() {
                timeLeft--

                // 通常のチャット通知 (10秒、5秒、4秒)
                if (timeLeft == 10 || timeLeft == 5 || timeLeft == 4) {
                    broadcastMessage(
                        Component.text("ゲーム開始まであと ", NamedTextColor.YELLOW)
                            .append(Component.text(timeLeft, NamedTextColor.RED, TextDecoration.BOLD))
                            .append(Component.text(" 秒", NamedTextColor.YELLOW))
                    )
                }

                // 残り3秒から大画面フォントを画面中央に叩き込む！
                when (timeLeft) {
                    3 -> sendCountdownTitle("\uE009") // countdown_3.png
                    2 -> sendCountdownTitle("\uE008") // countdown_2.png
                    1 -> sendCountdownTitle("\uE007") // countdown_1.png
                }

                // 0秒になったら終了してゲーム本編を始動
                if (timeLeft <= 0) {
                    cancel()
                    countdownTask = null
                    transitionToGame()
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

    /**
     * 画面中央にリソースパックのデカ文字フォントを送信する
     */
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

    private fun transitionToGame() {
        isGameRunning = true

        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))
        broadcastMessage(Component.text("       人狼ゲームが開始されました！       ", NamedTextColor.RED, TextDecoration.BOLD))
        broadcastMessage(Component.text("================================", NamedTextColor.GOLD))

        // オンライン中の参加者のみにゲーム開始メッセージを送信
        participants.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid) ?: return@forEach
            player.sendMessage(Component.text("あなたは「プレイヤー」としてゲームに参加します。", NamedTextColor.GREEN))
        }

        // オンライン中の観戦者のみにメッセージを送信
        spectators.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid) ?: return@forEach
            player.sendMessage(Component.text("あなたは「観戦者」としてゲームを視聴します。", NamedTextColor.YELLOW))
        }
    }

    /**
     * 全ステータスをリセットする
     */
    fun reset() {
        countdownTask?.cancel()
        countdownTask = null
        hostUniqueId = null
        rawParticipants.clear()
        rawSpectators.clear()
        rawNonParticipants.clear()
        isRecruiting = false
        isGameRunning = false
    }

    private fun broadcastMessage(component: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(component)
        }
    }
}