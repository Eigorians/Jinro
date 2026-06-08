package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.util.TargetFinder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Shufffler_Item : SpecialItem() {
    override val tag = "shuffler_2"
    override val lore = listOf("対象を\uE501で殺害 \uE500 + \uE501でスキル切り替え")
    override val material = Material.STICK

    override val role = Role.SHUFFLER
    override val displayName = role.roleName + "スキル 切り裂く　対象を\uE501で殺害 \uE500 + \uE501でスキル切り替え"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        // 冒頭：すでに使用不可フラグ（Byteの1）がPDCにあれば処理をキャンセルして終了
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0

            if (isCooltime == 1.toByte()) {

                if(player.isSneaking) {
                    player.inventory.setItemInMainHand(Shufffler_Avility_Item().createCooldownItem())
                }
                return
            }
            if(player.isSneaking) {
                player.inventory.setItemInMainHand(Shufffler_Avility_Item().create())
                return
            }
        }

        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()

        if (target !is ClickTarget.GamePlayerTarget) return

        val targetGamePlayer = target.gamePlayer

        val targetPlayer = targetGamePlayer.player ?: return

        val location = targetPlayer.location.add(0.0, 1.0, 0.0)

        targetPlayer.world.spawnParticle(Particle.SWEEP_ATTACK, location, 15, 0.2, 0.2, 0.2, 0.1)
        targetPlayer.world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f)

        // 提示いただいた順序を完全に維持：共通化したメソッドでクールダウンアイテムに入れ替える
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        // 条件判定とダメージ処理
        if (targetGamePlayer.role == Role.FOX || targetGamePlayer.Kishi || gamePlayer.role != this.role) return
        targetPlayer.damage(40.0 , player)
    }
}