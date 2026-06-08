package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
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
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration

object BlindnessToolItem : CustomItem() {
    override val id = "blind"
    override val material = Material.INK_SAC

    override val displayName = "盲目付与ツール(右クリックで使用)"

    override val description = listOf(
        "[アイテム説明]",
        "人狼役職以外に10秒間盲目を付与する",
        "一度使うと消滅する"
    )

    override val itemModel = "blindness_tool"
    override val hasRightClickEffect = true
    override val amount = 1

    // 💡 データパックの指定フォント "announce" を定義
    private val announceFont = Key.key("minecraft:announce")

    override fun create(amount: Int): ItemStack {
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("人狼役職以外に10秒間盲目を付与する", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
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

    override fun onRightClick(player: Player, itemStack: ItemStack): Boolean {
        val radius = 999.0 // 💡 データパックでは全プレイヤー対象(@a)のため、範囲を全体に広げています
        val durationTicks = 200L // 10秒間 (10s = 200ticks)

        // 💡 タイトルの表示時間設定（フェードイン 0秒 / 保持 3秒 / フェードアウト 0.5秒など。環境に合わせて微調整してください）
        val titleTimes = Title.Times.times(
            Duration.ofMillis(0),
            Duration.ofMillis(3000),
            Duration.ofMillis(500)
        )

        val targetPlayers = player.getNearbyEntities(radius, radius, radius)
            .filterIsInstance<Player>()

        // 💡 まず、ワールド内にいる生存プレイヤー全員にデータパック準拠のタイトル（アナウンス）を送信
        for (target in targetPlayers) {
            val gPlayer = JinroGameManager.gamePlayers[target.uniqueId] ?: continue
            if (!gPlayer.isAlive) continue

            // 💡 run.mcfunction のタイトルアナウンス分けを完全再現
            // 狼陣営（JINRO/KYOJIN）には \uE003 、それ以外には \uE004 を表示
            val announceChar = if (gPlayer.role.camp == Camp.JINRO) "\uE003" else "\uE004"

            val announceTitle = Title.title(
                Component.text(announceChar).font(announceFont),
                Component.empty(),
                titleTimes
            )
            target.showTitle(announceTitle)

            // 💡 狼・狂人以外のプレイヤーにのみ盲目(BLINDNESS)を付与
            if (target != player && gPlayer.role.camp != Camp.JINRO && gPlayer.camp != Camp.KYOJIN) {
                target.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, durationTicks.toInt(), 0, true, false))
            }
        }

        // 💡 10秒後に効果を解除するタイマー（defuse.mcfunction の完全再現）
        object : BukkitRunnable() {
            override fun run() {
                if (!JinroGameManager.isGameRunning) return

                // 全員のタイトルに解除アナウンス（\uE005）を表示
                for (target in org.bukkit.Bukkit.getOnlinePlayers()) {
                    val gPlayer = JinroGameManager.gamePlayers[target.uniqueId] ?: continue
                    if (!gPlayer.isAlive) continue

                    val defuseTitle = Title.title(
                        Component.text("\uE005").font(announceFont),
                        Component.empty(),
                        titleTimes
                    )
                    target.showTitle(defuseTitle)
                }
            }
        }.runTaskLater(Jinro.instance, durationTicks)

        player.sendMessage(Component.text("盲目付与ツールを使用しました。", NamedTextColor.GREEN))
        return true
    }
}