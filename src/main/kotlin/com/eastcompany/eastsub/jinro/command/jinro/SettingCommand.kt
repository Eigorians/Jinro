package com.eastcompany.eastsub.jinro.command

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class SettingCommand : SubCommand {
    override val name: String = "setting"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val sender = context.source.sender

                // ヘッダーメッセージ
                sender.sendMessage(Component.text("====== [人狼ゲーム設定] ======", NamedTextColor.GOLD))

                // 「・設定」はただのテキスト
                val titleText = Component.text("・設定", NamedTextColor.YELLOW)

                // 各種設定ボタン（ホバーイベントを削除）
                val jobBtn = createClickableMenu("  役職設定", "/jinroclick job")
                val shopBtn = createClickableMenu("  ショップ設定", "/jinroclick shop")
                val otherBtn = createClickableMenu("  その他の設定", "/jinroclick other")

                // メッセージの送信
                sender.sendMessage(titleText)
                sender.sendMessage(jobBtn)
                sender.sendMessage(shopBtn)
                sender.sendMessage(otherBtn)

                sender.sendMessage(Component.text("============================", NamedTextColor.GOLD))
                1
            }
    }

    /**
     * クリックするとコマンドを実行するテキストコンポーネントを生成するヘルパー関数
     */
    private fun createClickableMenu(text: String, command: String): Component {
        return Component.text(text, NamedTextColor.GREEN)
            .decoration(TextDecoration.UNDERLINED, true) // 下線を引く
            .clickEvent(ClickEvent.runCommand(command))  // クリック時のコマンドのみ残す
    }
}