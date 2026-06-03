package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager
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

                    // そもそも募集が開始されていない場合
                    if (!JinroMatchManager.isRecruiting) {
                        player.sendMessage(Component.text("現在、募集は行われていません。", NamedTextColor.RED))
                        return@executes 1
                    }

                    // 重複を防ぐため、一旦すべてのリストから除外
                    // 旧: JinroMatchManager.participants.remove(uuid) などを一括化
                    JinroGameManager.removeFromAllLists(uuid)

                    when (status) {
                        "participant" -> {
                            JinroGameManager.addParticipant(uuid)
                            player.sendMessage(Component.text("ステータスを「参加」に変更しました。", NamedTextColor.GREEN))
                        }
                        "spectator" -> {
                            JinroGameManager.addSpectator(uuid)
                            player.sendMessage(Component.text("ステータスを「観戦」に変更しました。", NamedTextColor.YELLOW))
                        }
                        "none" -> {
                            JinroGameManager.addNonParticipant(uuid)
                            player.sendMessage(Component.text("ステータスを「不参加」に変更しました。", NamedTextColor.GRAY))
                        }
                    }

                    val pCount = JinroGameManager.participants.size
                    val sCount = JinroGameManager.spectators.size
                    val nCount = JinroGameManager.nonParticipants.size
                    val selectedCount = pCount + sCount + nCount
                    val totalCount = JinroGameManager.getTotalRelevantPlayers()

                    val barLength = 10

                    // 💡 各ステータスに応じたバーのマス数を比率で計算
                    val pBars = if (totalCount > 0) (pCount * barLength) / totalCount else 0
                    val sBars = if (totalCount > 0) (sCount * barLength) / totalCount else 0
                    val nBars = if (totalCount > 0) (nCount * barLength) / totalCount else 0
                    val unreachedBars = barLength - (pBars + sBars + nBars)

                    // 💡 内訳ごとに色を変えてバーを組み立てる
                    val progressBar = Component.text("[", NamedTextColor.GRAY)
                        .append(Component.text("|".repeat(pBars), NamedTextColor.GREEN, TextDecoration.BOLD))      // 参加＝緑
                        .append(Component.text("|".repeat(sBars), NamedTextColor.YELLOW, TextDecoration.BOLD))     // 観戦＝黄
                        .append(Component.text("|".repeat(nBars), NamedTextColor.GRAY, TextDecoration.BOLD))       // 不参加＝灰
                        .append(Component.text("|".repeat(unreachedBars), NamedTextColor.DARK_GRAY))               // 未選択＝濃い灰
                        .append(Component.text("]", NamedTextColor.GRAY))

                    val actionBarMessage = Component.text("募集中 ", NamedTextColor.GOLD)
                        .append(progressBar)
                        .append(Component.text("  選択済み: ", NamedTextColor.WHITE))
                        .append(Component.text(selectedCount, NamedTextColor.GREEN, TextDecoration.BOLD))
                        .append(Component.text(" / ", NamedTextColor.GRAY))
                        .append(Component.text("$totalCount 人", NamedTextColor.AQUA))

                    for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                        val currentUuid = onlinePlayer.uniqueId
                        // 不参加リストに入っていない人、またはまだ未投票の人にのみ送る
                        if (!JinroMatchManager.nonParticipants.contains(currentUuid)) {
                            onlinePlayer.sendActionBar(actionBarMessage)
                        }
                    }

                    1
                }
            )
    }
}