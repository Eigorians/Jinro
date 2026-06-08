package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.command.SettingCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

object JinroCommand {

    private val settingCommand = SettingCommand()

    // 派生させたいサブコマンドのインスタンスをここにまとめる（インポート）
    private val subCommands = listOf<SubCommand>(
        StartCommand(),
        StopCommand(),
        settingCommand,
        MapCommand(),
        SpawnGraveCommand()
    )

    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        val mainCommand = Commands.literal("jinro")
            .executes { context ->
                settingCommand.execute(context.source.sender)
                1
            }

        // 各サブコマンドのツリーを親コマンドに結合（派生）していく
        for (sub in subCommands) {
            mainCommand.then(sub.register())
        }

        return mainCommand
    }
}