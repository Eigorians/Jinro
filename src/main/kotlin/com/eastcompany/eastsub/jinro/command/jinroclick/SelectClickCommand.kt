package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager // ✨ 募集管理はすべてここ
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class SelectClickCommand : SubCommand {
    override val name: String = "select"

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("status", StringArgumentType.word())
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes 1
                    val status = StringArgumentType.getString(context, "status")
                    val uuid = player.uniqueId

                    // 募集が開始されていない場合
                    if (!JinroMatchManager.isRecruiting) {
                        player.sendMessage(Component.text("現在、募集は行われていません。", NamedTextColor.RED))
                        return@executes 1
                    }

                    // 1. まず全てのリストから除外
                    JinroMatchManager.participants.remove(uuid)
                    JinroMatchManager.spectators.remove(uuid)
                    JinroMatchManager.nonParticipants.remove(uuid)

                    // 2. 選択されたステータスに追加
                    when (status) {
                        "participant" -> {
                            JinroMatchManager.participants.add(uuid)
                            player.sendMessage(Component.text("ステータスを「参加」に変更しました。", NamedTextColor.GREEN))
                        }
                        "spectator" -> {
                            JinroMatchManager.spectators.add(uuid)
                            player.sendMessage(Component.text("ステータスを「観戦」に変更しました。", NamedTextColor.YELLOW))
                        }
                        "none" -> {
                            JinroMatchManager.nonParticipants.add(uuid)
                            player.sendMessage(Component.text("ステータスを「不参加」に変更しました。", NamedTextColor.GRAY))
                        }
                    }

                    // 3. リアルタイム集計
                    val pCount = JinroMatchManager.participants.size
                    val sCount = JinroMatchManager.spectators.size
                    val nCount = JinroMatchManager.nonParticipants.size
                    val selectedCount = pCount + sCount + nCount
                    val totalCount = JinroMatchManager.getTotalRelevantPlayers()

                    // 💡 ✨【新設】全員が選択済みになったかどうかの判定
                    // 有効なプレイヤーが1人以上いて、その全員が選択を終えていたら自動スタート
                    if (totalCount > 0 && selectedCount >= totalCount) {
                        // 15秒カウントダウンを自動起動（内部でisRecruitingがfalseになり募集が締切られます）
                        JinroMatchManager.startCountdown()
                        return@executes 1 // ⚠️ カウントダウン側でアナウンスが流れるため、ここでの処理は即終了
                    }

                    // 4. バーの見た目計算（まだ全員が揃っていない場合のみ、ここが走る）
                    val barLength = 10
                    val pBars = if (totalCount > 0) (pCount * barLength) / totalCount else 0
                    val sBars = if (totalCount > 0) (sCount * barLength) / totalCount else 0
                    val nBars = if (totalCount > 0) (nCount * barLength) / totalCount else 0
                    val unreachedBars = barLength - (pBars + sBars + nBars)

                    val progressBar = Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text("|".repeat(pBars), NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text("|".repeat(sBars), NamedTextColor.YELLOW, TextDecoration.BOLD))
                        .append(Component.text("|".repeat(nBars), NamedTextColor.GRAY, TextDecoration.BOLD))
                        .append(Component.text("|".repeat(unreachedBars), NamedTextColor.DARK_GRAY))
                        .append(Component.text("]", NamedTextColor.GRAY))

                    val actionBarMessage = Component.text("募集中 ", NamedTextColor.GOLD)
                        .append(progressBar)
                        .append(Component.text("  選択済み: ", NamedTextColor.WHITE))
                        .append(Component.text(selectedCount, NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text(" / ", NamedTextColor.GRAY))
                        .append(Component.text("$totalCount 人", NamedTextColor.AQUA))

                    // 5. 全員へアクションバー更新
                    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                        if (!JinroMatchManager.nonParticipants.contains(onlinePlayer.uniqueId)) {
                            onlinePlayer.sendActionBar(actionBarMessage)
                        }
                    }

                    1
                }
            )
    }
}