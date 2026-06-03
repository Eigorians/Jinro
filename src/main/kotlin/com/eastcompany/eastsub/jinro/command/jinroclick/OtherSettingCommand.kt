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

class OtherSettingCommand : SubCommand {
    override val name: String = "other"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val sender = context.source.sender
                val config = plugin.configManager.gameConfig

                repeat(10) {
                    sender.sendMessage(Component.text(""))
                }

                sender.sendMessage(Component.text("\uF006\n", NamedTextColor.GRAY))

                // ─── 【時間設定】セクション ───
                sender.sendMessage(Component.text("【時間設定】", NamedTextColor.YELLOW))

                // 初日の昼時間 (デフォルト: 60)
                sender.sendMessage(
                    Component.text("  初日の昼時間 ")
                        .append(createArrowButton("<", "firstdaytime", "down"))
                        .append(Component.text(" ${config.firstDayTime}秒 ", getNumberColor(config.firstDayTime, 60)))
                        .append(createArrowButton(">", "firstdaytime", "up"))
                )

                // 昼時間 (デフォルト: 180)
                sender.sendMessage(
                    Component.text("  昼時間 ")
                        .append(createArrowButton("<", "daytime", "down"))
                        .append(Component.text(" ${config.dayTime}秒 ", getNumberColor(config.dayTime, 180)))
                        .append(createArrowButton(">", "daytime", "up"))
                )

                // 夜時間 (デフォルト: 60)
                sender.sendMessage(
                    Component.text("  夜時間 ")
                        .append(createArrowButton("<", "nighttime", "down"))
                        .append(Component.text(" ${config.nightTime}秒 ", getNumberColor(config.nightTime, 60)))
                        .append(createArrowButton(">", "nighttime", "up"))
                )

                // ─── 【ゲームルール設定】セクション ───
                sender.sendMessage(Component.text("【ゲームルール設定】", NamedTextColor.YELLOW))

                // 💡 ✨ 【変更】0人のときは「なし」、1人以上のときは「X人」にテキストを分岐
                val werewolfText = if (config.fixedWerewolfCount == 0) " なし " else " ${config.fixedWerewolfCount}人 "
                val werewolfColor = if (config.fixedWerewolfCount == 0) NamedTextColor.GRAY else getNumberColor(config.fixedWerewolfCount, 1)

                sender.sendMessage(
                    Component.text("  人狼役職の人数固定: ")
                        .append(createArrowButton("<", "werewolf", "down"))
                        .append(Component.text(werewolfText, werewolfColor))
                        .append(createArrowButton(">", "werewolf", "up"))
                )

                // 現在のマップ (デフォルト: "ノーマル")
                val mapColor = if (config.selectedMap == "ノーマル") NamedTextColor.WHITE else NamedTextColor.GREEN
                sender.sendMessage(
                    Component.text("  現在のマップ: ")
                        .append(Component.text("[ ", NamedTextColor.GRAY))
                        .append(Component.text(config.selectedMap, mapColor)
                            .decoration(TextDecoration.UNDERLINED, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick mapcycle")))
                        .append(Component.text(" ]", NamedTextColor.GRAY))
                )

                // ─── 戻るボタン ───
                val backButton = Component.text()
                    .append(Component.text("\n\uF003"))
                    .clickEvent(ClickEvent.runCommand("/jinro setting"))

                sender.sendMessage(backButton)
                1
            }
    }

    private fun createArrowButton(label: String, key: String, action: String): Component {
        return Component.text(label, NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .clickEvent(ClickEvent.runCommand("/jinroclick change $key $action"))
    }

    /**
     * 数値がデフォルト値より高いか、低いか、同じかによって適切な文字色を返す
     */
    private fun getNumberColor(current: Int, default: Int): NamedTextColor {
        return when {
            current > default -> NamedTextColor.RED
            current < default -> NamedTextColor.GREEN
            else -> NamedTextColor.WHITE
        }
    }
}