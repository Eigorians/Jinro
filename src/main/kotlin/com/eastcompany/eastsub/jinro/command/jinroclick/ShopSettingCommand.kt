package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin

class ShopSettingCommand : SubCommand {
    override val name: String = "shop"
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    // 表示名と内部キーのペア（並び順通り）
    private val items = listOf(
        "パン" to "bread_1", "パンx5" to "bread_5", "剣" to "wood_sword",
        "鉄の剣" to "iron_sword", "弓" to "bow", "矢" to "arrow",
        "斧" to "axe", "つるはし" to "pickaxe", "煙玉" to "smoke",
        "爆弾" to "bomb", "盲目付与" to "blind", "容姿統一ツール" to "disguise",
        "発光" to "glowing", "透明ポーション" to "invisibility"
    )

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { context ->
            val sender = context.source.sender
            val prices = plugin.configManager.gameConfig.shopPrices

            sender.sendMessage(Component.text("====== [ショップ価格設定] ======", NamedTextColor.GOLD))

            // 2項目ずつペアにして1行にまとめて出力（行数を半分に削減）
            for (i in items.indices step 2) {
                var row = Component.text("")

                // 左側のアイテム
                val left = items[i]
                row = row.append(createItemPriceNode(left.first, left.second, prices[left.second] ?: 0))

                // 右側のアイテム（奇数個の場合の安全対策）
                if (i + 1 < items.size) {
                    val right = items[i + 1]
                    // タブ代わりの空白を挟む
                    row = row.append(Component.text("   |   "))
                    row = row.append(createItemPriceNode(right.first, right.second, prices[right.second] ?: 0))
                }

                sender.sendMessage(row)
            }

            sender.sendMessage(Component.text("================================", NamedTextColor.GOLD))
            1
        }
    }

    /**
     * 「アイテム名 < 5G >」のUIパーツを作る
     */
    private fun createItemPriceNode(displayName: String, key: String, currentPrice: Int): Component {
        // 全体の幅を揃えやすくするため、全角スペースなどで位置調整しても綺麗になります
        val namePadding = displayName.padEnd(8, ' ')

        return Component.text(namePadding, NamedTextColor.YELLOW)
            .append(Component.text("<", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/jinroclick shopchange $key down")))
            .append(Component.text(" ${currentPrice}G ", NamedTextColor.WHITE))
            .append(Component.text(">", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/jinroclick shopchange $key up")))
    }
}