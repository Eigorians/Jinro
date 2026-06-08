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
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Saibankan_Item : SpecialItem() {
    override val tag = "saibankan_0"
    override val lore = listOf("対象を後任にする")
    override val material = Material.STICK

    override val role = Role.SAIBAN
    override val displayName = role.roleName + "スキル 後任指名"
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

        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        gamePlayer.abilityStatus = AbilityStatus.UNAVAILABLE
        player.sendMessage("あなたは ${targetPlayer.name} を後任に指名しました。")
        if(gamePlayer.role != role)return

        targetGamePlayer.isSaiban = true

        // 相手（クリックされた人物）へのメッセージ
        // 修正案
        targetPlayer.sendMessage("あなたは${player.name}により裁判官の後任に選ばれました。")
    }
}