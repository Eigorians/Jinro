package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin

class JobSelectCommand : SubCommand {
    override val name: String = "jobselect"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("roleEnum", StringArgumentType.word())
                .executes { context ->
                    val sender = context.source.sender
                    val roleEnumStr = StringArgumentType.getString(context, "roleEnum")

                    val role = runCatching { Role.valueOf(roleEnumStr) }.getOrNull()
                    if (role == null) {
                        sender.sendMessage(Component.text("不正な役職名です。", NamedTextColor.RED))
                        return@executes 1
                    }

                    val config = plugin.configManager.gameConfig
                    // マップから現在の人数を取得（登録がなければ0）
                    val currentCount = config.registeredRoles[role] ?: 0

                    sender.sendMessage(Component.text("====== [役職個別設定] ======", NamedTextColor.GOLD))
                    sender.sendMessage(Component.text("設定中: ${role.roleName} (${role.englishName})", NamedTextColor.YELLOW))

                    // 最大人数 < X人 > のUIを組み立て
                    val changeLine = Component.text("  最大人数 ")
                        .append(Component.text("<", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick jobchange ${role.name} down")))
                        .append(Component.text(" ${currentCount}人 ", NamedTextColor.WHITE))
                        .append(Component.text(">", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick jobchange ${role.name} up")))

                    sender.sendMessage(changeLine)
                    sender.sendMessage(Component.text(""))

                    // 戻るボタン
                    val backBtn = Component.text("[ 役職一覧に戻る ]", NamedTextColor.GRAY)
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick job"))
                    sender.sendMessage(backBtn)

                    sender.sendMessage(Component.text("============================", NamedTextColor.GOLD))
                    1
                }
            )
    }
}