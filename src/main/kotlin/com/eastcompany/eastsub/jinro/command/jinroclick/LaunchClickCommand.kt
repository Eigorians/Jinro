package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class LaunchClickCommand : SubCommand {
    override val name: String = "launch"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val player = context.source.sender as? Player ?: return@executes 1

                if (!JinroMatchManager.isRecruiting) {
                    player.sendMessage(Component.text("開始する募集がありません。", NamedTextColor.RED))
                    return@executes 1
                }

                // 主催者以外のプレイヤーがコマンド履歴等から不正に実行しようとした場合をガード
                if (JinroMatchManager.hostUniqueId != player.uniqueId) {
                    player.sendMessage(Component.text("ゲームを開始できるのは主催者のみです。", NamedTextColor.RED))
                    return@executes 1
                }

                // 参加者が誰も選ばれていない場合
                if (JinroMatchManager.participants.isEmpty()) {
                    player.sendMessage(Component.text("参加者が0人のため、ゲームを開始できません！", NamedTextColor.RED))
                    return@executes 1
                }

                // === 🚀 ゲーム本編の開始処理フェーズへ移行 ===
                JinroMatchManager.isRecruiting = false // 募集受付を終了

                Bukkit.broadcast(Component.text("人狼ゲームが開始されますロール（役職）を配布中...", NamedTextColor.GREEN))

                // TODO: ここに役職の抽選ロジックや、各スポーン地点（mapData.spawns）へのテレポート処理を繋げます

                1
            }
    }
}