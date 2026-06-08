package com.eastcompany.eastsub.jinro.item.job.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.TargetFinder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Uranaishi_Item : SpecialItem() {
    override val tag = "uranai_0"
    override val lore = listOf("対象を\uE501で占い、人狼か人間かを見分ける")
    override val material = Material.STICK // 必要に応じて変更してください

    override val role = Role.URANAI // 💡 占い師のRole（enum名に合わせて調整してください）
    override val displayName = role.roleName + "スキル 占う 対象を\uE501"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0

            if (isCooltime == 1.toByte()) {
                return
            }
        }

        // 4マス以内のターゲットを検索
        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()

        if (target !is ClickTarget.GamePlayerTarget) return

        val targetGamePlayer = target.gamePlayer
        val targetPlayer = targetGamePlayer.player ?: return

        // クールダウンアイテムに入れ替える（共通化メソッド）
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)


        if (gamePlayer.role != this.role) {
            player.sendMessage("§d[占い結果] ${targetPlayer.name} は §b人狼ではない §dようだ。")
            return
        }


        if (targetGamePlayer.role == Role.FOX) {
            player.sendMessage("§d[占い結果] ${targetPlayer.name} は §b人狼ではない §dようだ。")
            targetPlayer.damage(40.0 , player)
            targetPlayer.sendMessage("§cあなたは${player.name}に占われ、呪殺されてしまった…！")
            return
        }

        if (targetGamePlayer.role.camp == Camp.JINRO) {
            player.sendMessage("§d[占い結果] ${targetPlayer.name} は §c人狼 §dのようだ。")
        } else {
            player.sendMessage("§d[占い結果] ${targetPlayer.name} は §b人狼ではない §dようだ。")
        }
    }
}