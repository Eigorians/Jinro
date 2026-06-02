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

                sender.sendMessage(Component.text("====== [その他ゲーム設定] ======", NamedTextColor.GOLD))

                // 【時間設定】セクション
                sender.sendMessage(Component.text("【時間設定】", NamedTextColor.YELLOW))

                sender.sendMessage(
                    Component.text("  初日の昼時間 ")
                        .append(createArrowButton("<", "firstdaytime", "down"))
                        .append(Component.text(" ${config.firstDayTime}秒 ", NamedTextColor.WHITE))
                        .append(createArrowButton(">", "firstdaytime", "up"))
                        .append(createDefaultLabel(" (デフォルト: 60秒)")) // グレーの初期値表示
                )

                sender.sendMessage(
                    Component.text("  昼時間 ")
                        .append(createArrowButton("<", "daytime", "down"))
                        .append(Component.text(" ${config.dayTime}秒 ", NamedTextColor.WHITE))
                        .append(createArrowButton(">", "daytime", "up"))
                        .append(createDefaultLabel(" (デフォルト: 180秒)")) // グレーの初期値表示
                )

                sender.sendMessage(
                    Component.text("  夜時間 ")
                        .append(createArrowButton("<", "nighttime", "down"))
                        .append(Component.text(" ${config.nightTime}秒 ", NamedTextColor.WHITE))
                        .append(createArrowButton(">", "nighttime", "up"))
                        .append(createDefaultLabel(" (デフォルト: 60秒)")) // グレーの初期値表示
                )

                // 【ゲームルール設定】セクション
                sender.sendMessage(Component.text("【ゲームルール設定】", NamedTextColor.YELLOW))

                sender.sendMessage(
                    Component.text("  人狼役職の人数固定: ")
                        .append(createArrowButton("<", "werewolf", "down"))
                        .append(Component.text(" ${config.fixedWerewolfCount}人 ", NamedTextColor.WHITE))
                        .append(createArrowButton(">", "werewolf", "up"))
                        .append(createDefaultLabel(" (デフォルト: 1人)")) // グレーの初期値表示
                )

                sender.sendMessage(
                    Component.text("  フィールド ")
                        .append(Component.text("[ ", NamedTextColor.GRAY))
                        .append(Component.text(config.fieldType, NamedTextColor.GREEN)
                            .decoration(TextDecoration.UNDERLINED, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick change field cycle")))
                        .append(Component.text(" ]", NamedTextColor.GRAY))
                        .append(createDefaultLabel(" (デフォルト: default)")) // グレーの初期値表示
                )

                sender.sendMessage(Component.text("================================", NamedTextColor.GOLD))
                1
            }
    }

    /**
     * < や > の形をした、数値を増減させるためのクリックボタンを生成する
     */
    private fun createArrowButton(label: String, key: String, action: String): Component {
        return Component.text(label, NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .clickEvent(ClickEvent.runCommand("/jinroclick change $key $action"))
    }

    /**
     * 右側に表示するデフォルト値のグレーテキストを生成するヘルパー関数
     */
    private fun createDefaultLabel(text: String): Component {
        return Component.text(text, NamedTextColor.GRAY)
    }
}