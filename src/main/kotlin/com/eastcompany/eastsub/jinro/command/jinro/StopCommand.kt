package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.manager.JinroMatchManager
import com.eastcompany.eastsub.jinro.manager.JinroScoreboardManager
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit

class StopCommand : SubCommand {
    override val name = "stop"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val source = context.source

                // 1. 募集中、またはゲーム中であればマッチ管理データを完全リセット
                if (JinroMatchManager.isRecruiting || JinroMatchManager.participants.isNotEmpty()) {
                    JinroMatchManager.reset()

                    // 全員に強制終了（または募集キャンセル）をアナウンス
                    Bukkit.broadcast(
                        Component.text("人狼ゲーム（または参加募集）が管理者によって強制終了されました。", NamedTextColor.RED)
                    )
                } else {
                    // 何も動いていない時
                    source.sender.sendMessage(
                        Component.text("現在、開始されているゲームや募集はありません。", NamedTextColor.GRAY)
                    )
                }

                // TODO: もしすでにゲームが始まっていて、タイマー（BukkitTask）や
                // スコアボードなどを動かしている場合は、ここにそれらの停止・リセット処理も追記します。
                JinroScoreboardManager.clearBoard()
                1
            }
    }
}