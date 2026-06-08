package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.AbilityStatus
import com.eastcompany.eastsub.jinro.game.Camp
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

class Kaitou_Item : SpecialItem() {
    override val tag = "kaitou_0"
    override val lore = listOf("対象の役職を盗む")
    override val material = Material.STICK

    override val role = Role.KAITOU
    override val displayName = role.roleName + "スキル 役職強奪"
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

        //ポンコツだった場合
        if (gamePlayer.role != this.role) {
            val randomRole = Role.entries.filter { it != this.role }.random()
            player.sendMessage("あなたは ${targetPlayer.name} の役職を盗み、[${randomRole.roleName}] になりました。")
            JinroInventory().createRoleBook(gamePlayer,player,randomRole)
            return
        }

        // === 怪盗の固有処理（役職の強奪とメッセージ送信） ===
        val stolenRole = targetGamePlayer.role

        targetGamePlayer.role = Role.MURABITO
        targetGamePlayer.camp = Camp.VILLAGER
        if(gamePlayer.camp != Camp.LOVERS && gamePlayer.camp != Camp.FOX) {
            gamePlayer.camp = role.camp
        }

        // 自分の役職を相手の役職に書き換える
        gamePlayer.role = stolenRole

        player.sendMessage("あなたは ${targetPlayer.name} の役職を盗み、[${stolenRole.roleName}] になりました。")

        JinroInventory().createRoleBook(gamePlayer,player,gamePlayer.role)
    }
}