package com.eastcompany.eastsub.jinro.item.job.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.JinroPhase
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.Jinro_Item
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable

class Roar_Item : SpecialItem() {
    override val tag = "roar_0"
    override val lore = listOf("\uE501で咆哮スキルを発動します(射程20マス) \uE500 + \uE501でスキル切り替え")
    override val material = Material.BLAZE_ROD

    override val role = Role.JINRO
    override val displayName = role.roleName + "\uE501で咆哮 スキル(射程20マス) \uE500 + \uE501でスキル切り替え 夜のみ使用可能"
    override val model_key = "jinrou_0"
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand

        // クールダウンチェック
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0

            if (isCooltime == 1.toByte()) {
                if(player.isSneaking) {
                    player.inventory.setItemInMainHand(Jinro_Item().createCooldownItem())
                }
                return
            }
            if(player.isSneaking) {
                player.inventory.setItemInMainHand(Jinro_Item().create())
                return
            }
        }

        if(JinroGameManager.currentPhase != JinroPhase.NIGHT){
            player.sendMessage(Component.text("夜にしか使用できません", NamedTextColor.RED))
            return
        }

        // 予備動作演出（Wardenの怒り音）
        player.world.playSound(player.location, Sound.ENTITY_WARDEN_ANGRY, 3.0f, 1.0f)

        // ✅ 発動時にしっかりクールダウン用アイテムへ置き換える
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        var chargeTick = 0

        if(player.hasCooldown(material))return
        player.setCooldown(material, 600)

        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline || !JinroGameManager.isGameRunning) {
                    cancel()
                    return
                }

                if (chargeTick == 1) {
                    player.world.playSound(player.location, Sound.ENTITY_PLAYER_BREATH, 1.0f, 0.5f)
                    player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 20, 3, true, true))
                }

                if (chargeTick == 10) {
                    fireSonicBoom(player, gamePlayer)
                }

                chargeTick++

                if (chargeTick >= 15) {
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    /**
     * ソニックブーム弾丸のシミュレーション演算
     */
    private fun fireSonicBoom(launcher: Player, launcherGame: GamePlayer) {
        val plugin = Jinro.instance

        launcher.world.playSound(launcher.location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.5f)

        val eyeLoc = launcher.eyeLocation.add(0.0, 0.25, 0.0)
        val direction = launcher.location.direction.normalize()

        var currentDistance = 0.0
        val maxDistance = 20.0
        val speedPerTick = 1.5

        object : BukkitRunnable() {
            var currentPos = eyeLoc.clone()

            override fun run() {
                if (!JinroGameManager.isGameRunning) {
                    cancel()
                    return
                }

                currentPos.add(direction.clone().multiply(speedPerTick))
                currentDistance += speedPerTick

                // ✨ 発動者の陣営に関係なく、ワールド内の全員にソニックブームのエフェクトを見せる
                currentPos.world.spawnParticle(Particle.SONIC_BOOM, currentPos, 1, 0.0, 0.0, 0.0, 0.0)

                // 壁や障害物に当たったら消滅
                if (currentPos.block.type.isSolid) {
                    cancel()
                    return
                }

                // 周囲2マス以内のターゲット走査
                val targets = currentPos.world.getNearbyEntities(currentPos, 2.0, 2.0, 2.0)

                for (entity in targets) {
                    if (entity == launcher) continue
                    if (entity !is Player) continue // プレイヤー以外はスルー

                    val targetGamePlayer = JinroGameManager.gamePlayers[entity.uniqueId] ?: continue
                    if (!targetGamePlayer.isAlive) continue // スペクテーター等はスルー

                    // 一律で executeDamage へ流す（内部で人狼判定等を行いダメージを制御）
                    executeDamage(entity, targetGamePlayer, launcherGame)
                    cancel()
                    return
                }

                if (currentDistance >= maxDistance) {
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    /**
     * ダメージ判定とエフェクト処理
     */
    private fun executeDamage(targetPlayer: Player, targetGamePlayer: GamePlayer, gamePlayer: GamePlayer) {
        val location = targetPlayer.location.add(0.0, 1.0, 0.0)

        targetPlayer.world.spawnParticle(Particle.EXPLOSION, location, 3, 1.0, 1.0, 1.0, 0.0)

        if (targetGamePlayer.role == Role.FOX || targetGamePlayer.Kishi || gamePlayer.role != this.role) return

        targetPlayer.damage(40.0)
    }
}