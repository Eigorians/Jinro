package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.command.SettingCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object JinroCommand {

    // 派生させたいサブコマンドのインスタンスをここにまとめる（インポート）
    private val subCommands = listOf<SubCommand>(
        StartCommand(),
        StopCommand(),
        SettingCommand()
    )

    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        // 親コマンド "/jinro" の定義
        val mainCommand = Commands.literal("jinro")
            .executes { context ->
                // サブコマンドなしで /jinro だけ打たれた時の処理
                context.source.sender.sendMessage(Component.text("使用方法: /jinro [start|stop]", NamedTextColor.YELLOW))
                1
            }

        // 各サブコマンドのツリーを親コマンドに結合（派生）していく
        for (sub in subCommands) {
            mainCommand.then(sub.register())
        }

        return mainCommand
    }
}