package com.eastcompany.eastsub.jinro.command.jinro

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class StopCommand : SubCommand {
    override val name = "stop"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val source = context.source
                source.sender.sendMessage(Component.text("人狼ゲームを強制終了します。", NamedTextColor.RED))
                1
            }
    }
}