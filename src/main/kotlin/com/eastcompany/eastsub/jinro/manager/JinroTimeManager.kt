package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.game.JinroPhase
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager.nonParticipants
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameRules
import org.bukkit.World
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask

object JinroTimeManager {
    private val plugin: Jinro get() = Jinro.instance

    private var timeLeft: Int = 0
    private var maxTimeOfPhase: Int = 0

    private var timerTask: BukkitTask? = null
    private var bossBar: BossBar? = null

    private val endChecker = GameEndChecker(JinroGameManager)

    private val announceFont = Key.key("minecraft:announce")

    private const val TIME_DAY = 6000L       // 昼 (朝)
    private const val TIME_COURT = 6000L     // 裁判 (真昼)
    private const val TIME_NIGHT = 18000L    // 夜 (日没直後)

    /**
     * タイマーのスタート
     */
    fun startTimer() {
        stopTimer()
        val config = plugin.configManager.gameConfig

        JinroGameManager.currentPhase = JinroPhase.FIRST_DAY
        JinroGameManager.currentDayCount = 1

        timeLeft = config.firstDayTime
        maxTimeOfPhase = config.firstDayTime

        updateWorldTimeForPhase(JinroGameManager.currentPhase)

        bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID)
        Bukkit.getOnlinePlayers().forEach { bossBar?.addPlayer(it) }
        updateBossBarDisplay()

        timerTask = object : BukkitRunnable() {
            override fun run() {
                if (!JinroGameManager.isGameRunning) {
                    stopTimer()
                    return
                }

                // ─── 💡 修正ポイント: 戻り値を受け取らず、ただチェックを走らせる ───
                // 終了条件を満たしていた場合、この内部で自動的に endGame() と GameManager.reset() が実行されます。
                endChecker.checkGameEnd()

                // ゲームが終了（リセット）した場合は running フラグが false になるため、タイマーを即時停止して抜ける
                if (!JinroGameManager.isGameRunning) {
                    stopTimer()
                    return
                }

                if (timeLeft > 0) {
                    timeLeft--
                    updateBossBarDisplay()
                } else {
                    proceedToNextPhase()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    // 💡 削除: handleGameEnd(winners: List<Camp>)
    // リザルト表示やメッセージ送信、reset()の処理はすべて JinroGameEndManager に集約されたため不要になりました。

    /**
     * ⚙️ 次のフェーズへ状態遷移させる
     */
    private fun proceedToNextPhase() {
        val config = plugin.configManager.gameConfig

        when (JinroGameManager.currentPhase) {
            JinroPhase.FIRST_DAY -> {
                for((uuid, gamePlayer) in JinroGameManager.gamePlayers) {
                    gamePlayer.player?.let { plugin.itemManager.resetAllCooldowns(it) }
                }
                JinroGameManager.currentPhase = JinroPhase.NIGHT
                timeLeft = config.nightTime
                maxTimeOfPhase = config.nightTime
                JinroGameManager.respawnResource()
            }

            JinroPhase.NIGHT -> {
                val isSAIBANAvailable = JinroGameManager.gamePlayers.values.any {
                    it.isAlive && (it.role == Role.SAIBAN || it.isSaiban)
                }

                if (isSAIBANAvailable) {
                    JinroGameManager.currentPhase = JinroPhase.COURT
                    timeLeft = 60
                    maxTimeOfPhase = 60
                    val titleComponent = Component.text("\uE000").font(announceFont)
                    val title = Title.title(titleComponent, Component.empty())
                    val soundKey = Key.key("minecraft:start_trial")

                    val start_trial = net.kyori.adventure.sound.Sound.sound(
                        soundKey,
                        net.kyori.adventure.sound.Sound.Source.MASTER,
                        1.0f,
                        1.0f
                    )

                    for ((uuid, gamePlayer) in JinroGameManager.gamePlayers) {
                        if (nonParticipants.contains(uuid)) continue
                        Bukkit.getPlayer(uuid)?.showTitle(title)
                        Bukkit.getPlayer(uuid)?.playSound(start_trial)
                    }
                } else {
                    JinroGameManager.currentDayCount++
                    JinroGameManager.currentPhase = JinroPhase.DAY
                    timeLeft = config.dayTime
                    maxTimeOfPhase = config.dayTime
                }
                JinroGameManager.respawnResource()
            }

            JinroPhase.COURT -> {
                JinroGameManager.currentDayCount++
                JinroGameManager.currentPhase = JinroPhase.DAY
                timeLeft = config.dayTime
                maxTimeOfPhase = config.dayTime
            }

            JinroPhase.DAY -> {
                for((uuid, gamePlayer) in JinroGameManager.gamePlayers) {
                    gamePlayer.player?.let { plugin.itemManager.resetAllCooldowns(it) }
                }
                JinroGameManager.currentPhase = JinroPhase.NIGHT
                timeLeft = config.nightTime
                maxTimeOfPhase = config.nightTime
                JinroGameManager.respawnResource()
            }
        }

        updateWorldTimeForPhase(JinroGameManager.currentPhase)

        bossBar?.color = JinroGameManager.currentPhase.barColor
        updateBossBarDisplay()
    }

    /**
     * 🗺️ 指定されたフェーズに応じてワールド時間を変更する
     */
    private fun updateWorldTimeForPhase(phase: JinroPhase) {
        val world = getGameWorld() ?: return

        if (world.getGameRuleValue(GameRules.ADVANCE_TIME) == true) {
            world.setGameRule(GameRules.ADVANCE_TIME, false)
        }

        when (phase) {
            JinroPhase.FIRST_DAY -> world.time = TIME_DAY
            JinroPhase.DAY       -> world.time = TIME_DAY
            JinroPhase.COURT     -> world.time = TIME_COURT
            JinroPhase.NIGHT     -> world.time = TIME_NIGHT
        }
    }

    /**
     * 🗺️ 設定されたマップデータから対象のワールドのインスタンスを取得する
     */
    private fun getGameWorld(): World? {
        val config = plugin.configManager.gameConfig
        val mapData = config.mapData[config.selectedMap] ?: return null

        mapData.lobby?.world?.let { return it }
        mapData.spawns.firstOrNull()?.world?.let { return it }

        return null
    }

    private fun updateBossBarDisplay() {
        val bar = bossBar ?: return

        val phaseText = if (JinroGameManager.currentPhase == JinroPhase.FIRST_DAY || JinroGameManager.currentPhase == JinroPhase.DAY) "昼" else JinroGameManager.currentPhase.phaseName
        bar.setTitle("§f§l${JinroGameManager.currentDayCount}日目 ${phaseText}")

        if (maxTimeOfPhase > 0) {
            val progress = timeLeft.toDouble() / maxTimeOfPhase.toDouble()
            bar.progress = progress.coerceIn(0.0, 1.0)
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