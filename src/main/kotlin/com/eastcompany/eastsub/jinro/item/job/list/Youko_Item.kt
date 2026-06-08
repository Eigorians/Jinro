package com.eastcompany.eastsub.jinro.item.job.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.AbilityStatus
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.JinroInventory
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.util.TargetFinder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Youko_Item : SpecialItem() {
    override val tag = "youko_0"
    override val lore = listOf("対象を背教者に変える")
    override val material = Material.STICK

    override val role = Role.FOX
    override val displayName = role.roleName + "スキル 背教者化"
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

        // === 妖狐の固有処理（背教者化とメッセージ送信） ===

        // 相手の役職を背教者に書き換える（※Enumの定義名に合わせて必要があれば修正してください）

        // 自分（クリックした人物）へのメッセージ
        player.sendMessage("あなたは ${targetPlayer.name} を背教者に変えました。")
        gamePlayer.abilityStatus = AbilityStatus.UNAVAILABLE

        if(gamePlayer.role != role)return

        targetGamePlayer.camp = Camp.FOX

        // 相手（クリックされた人物）へのメッセージ
        val notificationComponent = Component.text()
            .append(Component.text("【役職変化】\n").color(NamedTextColor.RED))
            .append(Component.text("あなたは ").color(NamedTextColor.WHITE))
            .append(Component.text(player.name).color(NamedTextColor.GREEN))
            .append(Component.text(" によって").color(NamedTextColor.WHITE))
            .append(Component.text("背教者").color(NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("に変えられました。\n\n").color(NamedTextColor.WHITE))
            .append(Component.text("■ 勝利条件\n").color(NamedTextColor.GOLD))
            .append(Component.text("狐（妖狐）の生存").color(NamedTextColor.YELLOW))
            .build()

        targetPlayer.sendMessage(notificationComponent)

    }
}