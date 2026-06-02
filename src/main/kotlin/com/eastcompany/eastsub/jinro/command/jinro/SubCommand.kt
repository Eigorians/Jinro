package com.eastcompany.eastsub.jinro.command.jinro

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack

interface SubCommand {
    // 各サブコマンド名（"start", "stop" など）
    val name: String

    // サブコマンドの構造と処理を組み立てる関数
    fun register(): LiteralArgumentBuilder<CommandSourceStack>
}