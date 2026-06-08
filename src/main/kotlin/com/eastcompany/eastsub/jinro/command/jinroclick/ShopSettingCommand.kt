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

class ShopSettingCommand : SubCommand {
    override val name: String = "shop"
    private val plugin: Jinro get() = Jinro.instance

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
            val config = plugin.configManager.gameConfig
            val prices = config.shopPrices
            val defaultPrices = config.defaultShopPrices // ✨ 追加したデフォルト価格マップ

            repeat(10) {
                sender.sendMessage(Component.text(""))
            }

            sender.sendMessage(Component.text("\uF005\n"))
            // 2項目ずつペアにして1行にまとめて出力
            for (i in items.indices step 2) {
                var row = Component.text("")

                // 左側のアイテム
                val left = items[i]
                val leftCurrent = prices[left.second] ?: 0
                val leftDefault = defaultPrices[left.second] ?: 0
                row = row.append(createItemPriceNode(left.first, left.second, leftCurrent, leftDefault))

                // 右側のアイテム（奇数個の場合の安全対策）
                if (i + 1 < items.size) {
                    val right = items[i + 1]
                    val rightCurrent = prices[right.second] ?: 0
                    val rightDefault = defaultPrices[right.second] ?: 0
                    // タブ代わりの空白を挟む
                    row = row.append(Component.text("   |   "))
                    row = row.append(createItemPriceNode(right.first, right.second, rightCurrent, rightDefault))
                }

                sender.sendMessage(row)
            }

            // ─── 戻るボタンの追加 ───
            // fontの指定なし ＝ default.jsonの「\uF003」をそのまま呼び出し
            val backButton = Component.text()
                .append(Component.text("\n\uF003"))
                .clickEvent(ClickEvent.runCommand("/jinro setting"))

            sender.sendMessage(backButton)
            1
        }
    }

    /**
     * 「アイテム名 < 5G >」のUIパーツを作る
     * ✨ デフォルト値と比較して数字の色を動的に変更するロジックを組み込み
     */
    private fun createItemPriceNode(displayName: String, key: String, currentPrice: Int, defaultPrice: Int): Component {
        val namePadding = displayName.padEnd(8, ' ')

        // ─── 💡 ご要望の色判定 ───
        val priceColor = when {
            currentPrice > defaultPrice -> NamedTextColor.RED       // 高いなら薄緑
            currentPrice < defaultPrice -> NamedTextColor.GREEN // 低いならピンク
            else -> NamedTextColor.WHITE                               // デフォルト値なら白
        }

        return Component.text(namePadding, NamedTextColor.YELLOW)
            // 左側のボタン「<」をクリックで1G下げる
            .append(Component.text("<", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/jinroclick shopchange $key down")))
            // ✨ 中央の数字（現在の価格）の色を判定結果に置き換え
            .append(Component.text(" ${currentPrice}G ", priceColor))
            // 右側のボタン「>」をクリックで1G上げる
            .append(Component.text(">", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                .clickEvent(ClickEvent.runCommand("/jinroclick shopchange $key up")))
    }
}