package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.eastcompany.eastsub.jinro.manager.JinroGameManager       // ✨ 修正：新マネージャーに変更
import com.eastcompany.eastsub.jinro.manager.JinroScoreboardManager
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class JobChangeCommand : SubCommand {
    override val name: String = "jobchange"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("roleEnum", StringArgumentType.word())
                .then(Commands.argument("action", StringArgumentType.word())
                    .executes { context ->
                        val roleEnumStr = StringArgumentType.getString(context, "roleEnum")
                        val action = StringArgumentType.getString(context, "action")

                        val role = runCatching { Role.valueOf(roleEnumStr) }.getOrNull() ?: return@executes 1
                        val manager = plugin.configManager
                        val config = manager.gameConfig

                        // 既存のイミュータブルなMapを MutableMap に変換して操作する
                        val currentMap = config.registeredRoles.toMutableMap()
                        val currentCount = currentMap[role] ?: 0

                        if (action == "up") {
                            currentMap[role] = currentCount + 1
                        } else if (action == "down") {
                            val nextCount = currentCount - 1
                            if (nextCount > 0) {
                                currentMap[role] = nextCount
                            } else {
                                // 0人以下になる場合はMapからキーごと削除（Configをスッキリさせるため）
                                currentMap.remove(role)
                            }
                        }

                        // 新しいMapを設定に反映させてセーブ
                        config.registeredRoles = currentMap
                        manager.save()

                        // ✨ 修正：JinroGameManagerのフラグを参照するように変更
                        if (JinroGameManager.isRecruiting) {
                            JinroScoreboardManager.displayRecruitBoard()
                        }

                        // dispatchCommand を使って現在の個別設定画面を安全に再描画（連打対応）
                        val server = Bukkit.getServer()
                        server.dispatchCommand(context.source.sender, "jinroclick jobselect ${role.name}")
                        1
                    }
                )
            )
    }
}