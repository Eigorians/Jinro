package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.manager.JinroMatchManager  // ✨ 戻した
import com.eastcompany.eastsub.jinro.manager.JinroGameManager   // ✨ 併用
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

                // 💡 募集中（カウントダウン含む）か、本番ゲーム中であれば検知
                if (JinroMatchManager.isRecruiting || JinroGameManager.isGameRunning) {

                    // 両方のマネージャーを安全に停止・リセット
                    JinroMatchManager.reset() // 15秒カウントダウンタスクやボスバーもここで自動消滅
                    JinroGameManager.reset()  // 稼働中の本番プレイヤーデータ等を初期化

                    // 全員に強制終了をアナウンス
                    Bukkit.broadcast(
                        Component.text("人狼ゲーム（または参加募集）が管理者によって強制終了されました。", NamedTextColor.RED)
                    )
                } else {
                    // 何も動いていない時
                    source.sender.sendMessage(
                        Component.text("現在、開始されているゲームや募集はありません。", NamedTextColor.GRAY)
                    )
                }

                // スコアボードのクリア
                JinroScoreboardManager.clearBoard()
                1
            }
    }
}