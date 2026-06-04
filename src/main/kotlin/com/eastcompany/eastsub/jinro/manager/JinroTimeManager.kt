package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.game.JinroPhase
import com.eastcompany.eastsub.jinro.game.Role
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object JinroTimeManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    var currentPhase: JinroPhase = JinroPhase.FIRST_DAY
    var currentDayCount: Int = 1
    private var timeLeft: Int = 0
    private var maxTimeOfPhase: Int = 0

    private var timerTask: BukkitTask? = null
    private var bossBar: BossBar? = null

    private val endChecker = GameEndChecker(JinroGameManager)

    /**
     * タイマーのスタート（中略 - 以前のコードのまま）
     */
    fun startTimer() {
        stopTimer()
        val config = plugin.configManager.gameConfig
        currentPhase = JinroPhase.FIRST_DAY
        currentDayCount = 1
        timeLeft = config.firstDayTime
        maxTimeOfPhase = config.firstDayTime

        bossBar = Bukkit.createBossBar("", BarColor.PINK, BarStyle.SOLID)
        Bukkit.getOnlinePlayers().forEach { bossBar?.addPlayer(it) }
        updateBossBarDisplay()

        timerTask = object : BukkitRunnable() {
            override fun run() {
                if (!JinroGameManager.isGameRunning) {
                    stopTimer()
                    return
                }

                val winners = endChecker.checkGameEnd()
                if (winners.isNotEmpty()) {
                    handleGameEnd(winners)
                    return
                }

                if (timeLeft > 0) {
                    timeLeft--
                    updateBossBarDisplay()
                    if (timeLeft <= 5 && timeLeft > 0) {
                        Bukkit.getOnlinePlayers().forEach { p -> p.playSound(p.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f) }
                    }
                } else {
                    proceedToNextPhase()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    private fun handleGameEnd(winners: List<Camp>) {
        stopTimer()
        val winnersStr = winners.joinToString(", ") { it.name }
        val endComponent = Component.text("\n========================================\n", NamedTextColor.GOLD)
            .append(Component.text("🎉 ゲームが終了しました！ 🎉\n\n", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text("【勝利陣営】: ", NamedTextColor.WHITE))
            .append(Component.text(winnersStr, NamedTextColor.GREEN, TextDecoration.BOLD))
            .append(Component.text("\n========================================\n", NamedTextColor.GOLD))

        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(endComponent)
            player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f)
        }
        JinroGameManager.reset()
    }

    /**
     * ⚙️ 次のフェーズへ状態遷移させる（条件チェック判定付き）
     */
    private fun proceedToNextPhase() {
        val config = plugin.configManager.gameConfig

        when (currentPhase) {
            JinroPhase.FIRST_DAY -> {
                currentPhase = JinroPhase.NIGHT
                timeLeft = config.nightTime
                maxTimeOfPhase = config.nightTime
                announcePhaseStart("🌙 夜の行動が開始されました。人狼や役職者は能力を使用してください。", NamedTextColor.BLUE)
            }

            JinroPhase.NIGHT -> {
                // 💡 ✨【新設】裁判フェーズへ入るための条件チェック
                // 生存しているプレイヤーの中で「本来の役職が裁判官」または「裁判官後任フラグ(isSaiban)がtrue」の人がいるか
                val isJudgeAvailable = JinroGameManager.gamePlayers.values.any {
                    it.isAlive && (it.role == Role.JUDGE || it.isSaiban) // ※Role.JUDGE の名前は環境に合わせて適宜修正してください
                }

                if (isJudgeAvailable) {
                    // ⭕ 条件成立：通常の裁判フェーズへ
                    currentPhase = JinroPhase.COURT
                    timeLeft = 60
                    maxTimeOfPhase = 60
                    announcePhaseStart("⚖ 裁判の時間です。投票を行ってください。", NamedTextColor.RED)
                } else {
                    // ❌ 条件未達成：裁判官が全滅しているため、裁判をスキップして即座に「次の日の昼」へ！
                    currentDayCount++
                    currentPhase = JinroPhase.DAY
                    timeLeft = config.dayTime
                    maxTimeOfPhase = config.dayTime

                    announcePhaseStart(
                        "⚖ 裁判官が不在（死亡）のため、本日の裁判は行われません。\n☀️ ${currentDayCount}日目の朝が来ました。議論を開始してください。",
                        NamedTextColor.YELLOW
                    )
                }
            }

            JinroPhase.COURT -> {
                // 裁判終了 ➡️ 次の日の昼へ
                currentDayCount++
                currentPhase = JinroPhase.DAY
                timeLeft = config.dayTime
                maxTimeOfPhase = config.dayTime
                announcePhaseStart("☀️ ${currentDayCount}日目の朝が来ました。議論を開始してください。", NamedTextColor.YELLOW)
            }

            JinroPhase.DAY -> {
                currentPhase = JinroPhase.NIGHT
                timeLeft = config.nightTime
                maxTimeOfPhase = config.nightTime
                announcePhaseStart("🌙 夜が訪れました。各自警戒してください。", NamedTextColor.BLUE)
            }
        }

        bossBar?.color = currentPhase.barColor
        updateBossBarDisplay()
    }

    private fun updateBossBarDisplay() {
        val bar = bossBar ?: return
        val minutes = timeLeft / 60
        val seconds = timeLeft % 60
        val timeStr = String.format("%02d:%02d", minutes, seconds)

        val phaseText = if (currentPhase == JinroPhase.FIRST_DAY || currentPhase == JinroPhase.DAY) "昼" else currentPhase.phaseName
        bar.setTitle("§f§l${currentDayCount}日目　${phaseText} [§b${timeStr}§f]")

        if (maxTimeOfPhase > 0) {
            val progress = timeLeft.toDouble() / maxTimeOfPhase.toDouble()
            bar.progress = progress.coerceIn(0.0, 1.0)
        }
    }

    private fun announcePhaseStart(message: String, color: NamedTextColor) {
        val textComponent = Component.text("========================================", NamedTextColor.DARK_GRAY)
            .append(Component.text("\n$message\n", color, TextDecoration.BOLD))
            .append(Component.text("========================================", NamedTextColor.DARK_GRAY))

        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(textComponent)
        }
    }

    fun stopTimer() {
        timerTask?.cancel()
        timerTask = null
        bossBar?.removeAll()
        bossBar = null
    }

    fun getBossBar(): BossBar? {
        return bossBar
    }
}