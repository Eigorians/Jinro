package com.eastcompany.eastsub.jinro.command.jinro

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class StartCommand : SubCommand {
    override val name = "start"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val source = context.source
                source.sender.sendMessage(Component.text("人狼ゲームを開始します！", NamedTextColor.GREEN))

                // コマンドの実行結果（成功は1、失敗は0やマイナス）
                1
            }
    }
}