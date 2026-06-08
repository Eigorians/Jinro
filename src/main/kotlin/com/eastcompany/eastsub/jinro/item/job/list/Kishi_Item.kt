package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.AbilityStatus
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

class Kishi_Item : SpecialItem() {
    override val tag = "kishi_0"
    override val lore = listOf("対象を\uE501で護衛")
    override val material = Material.STICK

    override val role = Role.KISHI
    override val displayName = role.roleName + "スキル 守護 対象を\uE501で護衛"
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
                return
            }
        }

        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()

        if (target !is ClickTarget.GamePlayerTarget) return

        val targetGamePlayer = target.gamePlayer

        val targetPlayer = targetGamePlayer.player ?: return

        // 提示いただいた順序を完全に維持：共通化したメソッドでクールダウンアイテムに入れ替える
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        player.sendMessage("あなたは ${targetPlayer.name} を護衛した。")

        if(gamePlayer.role != role)return

        targetGamePlayer.Kishi = true
    }
}