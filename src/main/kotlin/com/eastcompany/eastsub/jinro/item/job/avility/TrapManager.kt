package com.eastcompany.eastsub.jinro.game.trap

import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.item.job.avility.TrapData
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.cos
import kotlin.math.sin

class TrapManager(private val plugin: JavaPlugin) {

    // ブロック座標をキーにして罠データを保持
    private val traps = ConcurrentHashMap<Location, TrapData>()

    // 💡 変更: プレイヤーUUIDをキーに、[残りTick数, 罠の設置座標(Location)] を保持するように変更
    private val trappedPlayers = ConcurrentHashMap<UUID, Pair<Int, Location>>()

    // タスクをキャンセルできるように変数で保持
    private var tickerTask: BukkitRunnable? = null

    init {
        startTrapTicker()
    }

    /**
     * 外部から罠を設置するAPIメソッド
     */
    fun deployTrap(player: Player, clickedBlock: Block, isDummy: Boolean): Boolean {
        // アイテム側ですでに1つ上のブロックが渡されているため、そのまま location を使用
        val loc = clickedBlock.location

        // 1人当たりの最大設置数制限 (上限4個) をチェック
        val currentPlacedCount = traps.values.count { it.ownerUuid == player.uniqueId }
        if (currentPlacedCount >= 4) {
            player.sendMessage("罠はこれ以上設置できない (現在の設置数: ${currentPlacedCount}個、上限: 4個)")
            return false
        }

        // 重複設置のチェック
        if (traps.containsKey(loc)) {
            player.sendMessage("ここには既に罠が設置されています")
            return false
        }

        // 罠データの作成と登録
        val trap = TrapData(
            location = loc,
            ownerUuid = player.uniqueId,
            isDummy = isDummy,
            isInactive = true // 離れるまで未起動
        )
        traps[loc] = trap

        // 設置時のSE再生
        loc.world.playSound(loc, Sound.ENTITY_PLAYER_BIG_FALL, 1.0f, 1.0f)
        player.sendMessage("罠を設置した (現在の設置数: ${currentPlacedCount + 1}個 / 上限: 4個)")

        return true
    }

    /**
     * 毎Tick処理（スケジューラー）
     */
    private fun startTrapTicker() {
        tickerTask?.cancel()

        tickerTask = object : BukkitRunnable() {
            override fun run() {
                // 1. 本物の罠にハマっているプレイヤーの拘束処理 (毎Tickテレポート)
                handleTrappedPlayers()

                val iterator = traps.values.iterator()
                while (iterator.hasNext()) {
                    val trap = iterator.next()
                    val centerLoc = trap.location.clone().add(0.5, 0.0, 0.5)

                    // A. 設置者が離れると罠が「アクティブ」になる判定
                    if (trap.isInactive) {
                        val owner = Bukkit.getPlayer(trap.ownerUuid)
                        if (owner == null || centerLoc.distance(owner.location) > 1.3) {
                            trap.isInactive = false
                        }
                    }

                    // B. パーティクルで見せる処理
                    showTrapParticles(trap, centerLoc)

                    // C. 罠の発動チェック
                    if (!trap.isInactive) {
                        if (trap.isDummy) {
                            continue
                        }

                        val nearbyEntities = centerLoc.world.getNearbyEntities(centerLoc, 0.5, 0.5, 0.5)

                        val victim = nearbyEntities
                            .filterIsInstance<Player>()
                            .mapNotNull { JinroGameManager.gamePlayers[it.uniqueId] }
                            .firstOrNull { it.isAlive && it.player != null }

                        if (victim != null) {
                            triggerTrap(victim.player!!, trap, centerLoc)
                            iterator.remove() // 踏まれたので削除
                        }
                    }
                }
            }
        }
        tickerTask?.runTaskTimer(plugin, 0L, 1L)
    }

    /**
     * 💡 【修正】落とし穴にハマっているプレイヤーの拘束タスク
     * 罠にかかっている間、トラップの1マス下に毎Tick強制テレポートさせ続けます。
     */
    private fun handleTrappedPlayers() {
        val pIterator = trappedPlayers.entries.iterator()
        while (pIterator.hasNext()) {
            val entry = pIterator.next()
            val player = Bukkit.getPlayer(entry.key)
            if (player == null) {
                pIterator.remove()
                continue
            }

            val gp = JinroGameManager.gamePlayers[player.uniqueId]
            if (gp == null || !gp.isAlive) {
                pIterator.remove()
                continue
            }

            val (ticksLeft, trapLoc) = entry.value

            if (ticksLeft > 0) {
                // 💡 トラップの座標から Y を -1.0 した位置（中心に合わせるため X, Z に +0.5）
                val tpTarget = trapLoc.clone().add(0.5, -1.0, 0.5)

                // プレイヤーの現在の首の向き（Yaw/Pitch）を維持して固定する
                tpTarget.yaw = player.location.yaw
                tpTarget.pitch = player.location.pitch

                // 毎Tickテレポートを実行
                player.teleport(tpTarget)

                // 残り時間を減らす
                if (ticksLeft > 1) {
                    trappedPlayers[player.uniqueId] = Pair(ticksLeft - 1, trapLoc)
                } else {
                    // 💡 解放時: 落とし穴から地上（元の罠の高さ）へ引き上げる
                    player.teleport(trapLoc.clone().add(0.5, 0.0, 0.5).apply {
                        yaw = player.location.yaw
                        pitch = player.location.pitch
                    })
                    pIterator.remove()
                }
            }
        }
    }

    /**
     * 罠発動時のエフェクト・デバフ付与
     */
    private fun triggerTrap(player: Player, trap: TrapData, loc: Location) {
        // 💡 変更: 100本のカウントとともに、罠の設置座標（trap.location）を一緒に記憶させる
        trappedPlayers[player.uniqueId] = Pair(100, trap.location)

        val functionPath = "animated_java:pitfall/summon"
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "execute at ${player.name} run function $functionPath")

        loc.world.spawnParticle(Particle.CLOUD, loc.clone().add(1.5, 1.5, 1.5), 5, 1.0, 1.0, 1.0, 0.0, null, true)
        loc.world.spawnParticle(Particle.EXPLOSION, loc, 1)

        loc.world.getNearbyEntities(loc, 5.0, 5.0, 5.0).forEach { entity ->
            if (entity is Player) {
                entity.playSound(loc, Sound.ENTITY_PLAYER_BIG_FALL, 1.0f, 1.0f)
            }
        }

        player.sendMessage("あなたは落とし穴に落ちてしまった…!")
        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 100, 250, true, false, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 127, true, false, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 100, 255, true, false, true))
        player.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 100, 255, true, false, true))
    }

    /**
     * 陣営制限に基づいた可視化パーティクルの送信
     */
    private fun showTrapParticles(trap: TrapData, loc: Location) {
        if (trap.isInactive) return

        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            val gp = JinroGameManager.gamePlayers[onlinePlayer.uniqueId]
            var shouldSee = false

            val isSpectator = gp == null || !gp.isAlive

            if (!trap.isDummy) {
                if (isSpectator) {
                    shouldSee = true
                } else {
                    if (gp.role.camp == Camp.JINRO || gp.role.camp == Camp.KYOJIN) {
                        shouldSee = true
                    }
                }
            } else {
                if (isSpectator || onlinePlayer.uniqueId == trap.ownerUuid) {
                    shouldSee = true
                }
            }

            if (shouldSee) {
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val xOffset = cos(angle) * 0.75
                    val zOffset = sin(angle) * 0.75
                    onlinePlayer.spawnParticle(
                        Particle.SMOKE,
                        loc.clone().add(xOffset, 0.1, zOffset),
                        1, 0.0, 0.0, 0.0, 0.0
                    )
                }
            }
        }
    }

    /**
     * ゲーム終了（リセット）時のクリーンアップ処理
     */
    fun clearAllTraps() {
        tickerTask?.cancel()
        tickerTask = null
        traps.clear()
        trappedPlayers.clear()
    }
}