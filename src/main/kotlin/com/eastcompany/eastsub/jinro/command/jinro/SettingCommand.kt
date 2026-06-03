package com.eastcompany.eastsub.jinro.command

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

class SettingCommand : SubCommand {
    override val name: String = "setting"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val sender = context.source.sender

                repeat(11) {
                    sender.sendMessage(Component.text(""))
                }


                val titleText = Component.text("\uF002\n")

                sender.sendMessage("\n")

                // 各種設定ボタン
                val mapBtn = createClickableMenu("\uF020", "/jinro map", null)
                val jobBtn = createClickableMenu("\uF004", "/jinroclick job", null)
                val shopBtn = createClickableMenu("\uF005", "/jinroclick shop", null)
                val otherBtn = createClickableMenu("\uF006", "/jinroclick other", null)

                // ✨ 同じ行に並べるためのコンポーネント結合
                val controlRow = Component.text("  ") // インデント
                    .append(
                        Component.text("[開始]", NamedTextColor.GREEN)
                            .clickEvent(ClickEvent.runCommand("/jinro start"))
                    )
                    .append(Component.text(" / ", NamedTextColor.GRAY)) // 区切り（スラッシュ）
                    .append(
                        Component.text("[停止]", NamedTextColor.RED)
                            .clickEvent(ClickEvent.runCommand("/jinro stop"))
                    )

                // メッセージの送信
                sender.sendMessage(titleText)
                sender.sendMessage(mapBtn)
                sender.sendMessage(jobBtn)
                sender.sendMessage(shopBtn)
                sender.sendMessage(otherBtn)

                sender.sendMessage(Component.text("")) // 調整スペース

                sender.sendMessage(controlRow) // ✨ 開始 / 停止 を1行で送信

                sender.sendMessage("")
                1
            }
    }

    private fun createClickableMenu(text: String, command: String, textColor: NamedTextColor?): Component {
        return Component.text(text, textColor)
            .clickEvent(ClickEvent.runCommand(command))
    }
}