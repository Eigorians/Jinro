package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

object JinroClickCommand {

    // 新しく作った3つの役職関連コマンドをリストに追加
    private val subCommands = listOf<SubCommand>(
        OtherSettingCommand(),
        ChangeSettingCommand(),
        JobSettingCommand(),   // 追加
        JobSelectCommand(),    // 追加
        JobChangeCommand(),    // 追加
        ShopSettingCommand(), // ⚠️追加: /jinroclick shop
        ShopChangeCommand()
    )

    fun create(): LiteralArgumentBuilder<CommandSourceStack> {
        val mainCommand = Commands.literal("jinroclick")
            .executes { context ->
                context.source.sender.sendMessage(
                    Component.text("内部エラー: 有効な項目が選択されていません。", NamedTextColor.RED)
                )
                1
            }

        for (sub in subCommands) {
            mainCommand.then(sub.register())
        }

        return mainCommand
    }
}