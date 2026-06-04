package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager  // ✨ カウントダウンまではこっち
import com.eastcompany.eastsub.jinro.manager.JinroGameManager   // ✨ ゲーム中の重複防止チェック用
import com.eastcompany.eastsub.jinro.manager.JinroScoreboardManager
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class StartCommand : SubCommand {
    override val name: String = "start"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val sender = context.source.sender
                if (sender !is Player) {
                    sender.sendMessage(Component.text("このコマンドはプレイヤーのみ実行できます。", NamedTextColor.RED))
                    return@executes 1
                }

                // 💡 1. 既に本編ゲームが走っている場合は重複起動をブロック
                if (JinroGameManager.isGameRunning) {
                    sender.sendMessage(Component.text("既にゲームは進行中です。", NamedTextColor.RED))
                    return@executes 1
                }

                // 💡 2. 既に募集中なら、15秒カウントダウンシーケンス（MatchManager側）へ移行
                if (JinroMatchManager.isRecruiting) {
                    if (JinroMatchManager.hostUniqueId != sender.uniqueId) {
                        sender.sendMessage(Component.text("募集を開始したホストのみがゲームをスタートできます。", NamedTextColor.RED))
                        return@executes 1
                    }

                    // MatchManagerのカウントダウンを起動！（0秒になると自動でGameManagerへバトンタッチします）
                    JinroMatchManager.startCountdown()
                    return@executes 1
                }

                // ─── 3. 初回実行時：募集フェーズの初期化（MatchManager） ───
                JinroMatchManager.reset()
                JinroMatchManager.isRecruiting = true
                JinroMatchManager.hostUniqueId = sender.uniqueId

                // サイドバーに設定されている役職と人数を一括表示
                JinroScoreboardManager.displayRecruitBoard()

                // 全員へ送る募集チャットUIの作成
                val title = Component.text("========= [人狼ゲーム参加募集] =========", NamedTextColor.GOLD)

                val buttons = Component.text("  あなたの状態を選択してください:\n\n    ")
                    .append(Component.text("[ 🟢 参加 ]", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick select participant")))
                    .append(Component.text("    "))
                    .append(Component.text("[ 🟡 観戦 ]", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick select spectator")))
                    .append(Component.text("    "))
                    .append(Component.text("[ ❌ 不参加 ]", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick select none")))

                val footer = Component.text("\n===================================", NamedTextColor.GOLD)

                // 全プレイヤーにアンケートを配信
                for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendMessage(title)
                    onlinePlayer.sendMessage(buttons)
                    onlinePlayer.sendMessage(footer)
                }

                1
            }
    }
}