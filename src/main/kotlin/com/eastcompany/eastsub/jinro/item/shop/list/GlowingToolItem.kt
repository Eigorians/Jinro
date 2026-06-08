package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.item.CustomItem
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.Duration

object GlowingToolItem : CustomItem() {
    override val id = "glowing"
    override val material = Material.GLOW_INK_SAC

    override val displayName = "発光ツール(右クリックで使用)"

    override val description = listOf(
        "[アイテム説明]",
        "全員が10秒間光り輝く",
        "一度使うと消滅する"
    )

    override val itemModel = "glowing_tool"
    override val hasRightClickEffect = true
    override val amount = 1

    // 💡 データパック共通の指定フォント "announce" を定義
    private val announceFont = Key.key("minecraft:announce")

    override fun create(amount: Int): ItemStack {
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("全員が10秒間光り輝く", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("一度使うと消滅する", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        )
        meta.lore(cleanedLore)

        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }

    /**
     * 💡 右クリックしたときの発光付与ロジック (データパック完全再現版)
     */
    override fun onRightClick(player: Player, itemStack: ItemStack): Boolean {
        val durationTicks = 200L // 10秒間 (10s = 200ticks)

        // 💡 タイトルの表示時間設定（フェードイン 0秒 / 保持 3秒 / フェードアウト 0.5秒）
        val titleTimes = Title.Times.times(
            Duration.ofMillis(0),
            Duration.ofMillis(3000),
            Duration.ofMillis(500)
        )

        // 💡 ワールド内の全生存プレイヤーに処理を及ぼす
        // (データパックの @a[tag=player] および @a run title と完全に同期)
        val targetPlayers = player.world.players

        for (target in targetPlayers) {
            val gPlayer = JinroGameManager.gamePlayers[target.uniqueId] ?: continue

            // 人狼ゲームに参加しており、かつ生きているプレイヤーのみが対象
            if (!gPlayer.isAlive) continue

            // 💡 1. 10秒間の発光(GLOWING)を付与（パーティクルは非表示にするため最後を true, false に設定）
            target.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, durationTicks.toInt(), 0, true, false))

            // 💡 2. データパック指定のタイトル（アナウンス \uE006 ）を全生存者に送信
            val glowTitle = Title.title(
                Component.text("\uE006").font(announceFont),
                Component.empty(),
                titleTimes
            )
            target.showTitle(glowTitle)
        }

        player.sendMessage(Component.text("発光ツールを使用しました。", NamedTextColor.GREEN))
        return true // 成功を返し、手持ちのアイテムを1個消費させて消滅
    }
}