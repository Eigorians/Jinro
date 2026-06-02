package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.plugin.java.JavaPlugin

class JobSettingCommand : SubCommand {
    override val name: String = "job"
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { context ->
            val sender = context.source.sender
            val registeredRoles = plugin.configManager.gameConfig.registeredRoles

            sender.sendMessage(Component.text("====== [役職設定一覧] ======", NamedTextColor.GOLD))

            Camp.entries.forEach { camp ->
                val campRoles = Role.entries.filter { it.camp == camp }
                if (campRoles.isEmpty()) return@forEach

                val campColor = when (camp) {
                    Camp.WEREWOLF -> NamedTextColor.RED
                    Camp.VILLAGER -> NamedTextColor.GREEN
                    Camp.FOX -> NamedTextColor.LIGHT_PURPLE
                    Camp.TERUTERU -> NamedTextColor.YELLOW
                    Camp.LOVERS -> NamedTextColor.AQUA
                    Camp.GRIM_REAPER -> NamedTextColor.DARK_GRAY
                }

                // 1行にまとめて並べるためのコンポーネントを初期化
                var rowComponent = Component.text("【${camp.name}】", campColor)

                campRoles.forEach { role ->
                    val count = registeredRoles[role] ?: 0
                    val countColor = if (count > 0) NamedTextColor.GOLD else NamedTextColor.GRAY

                    // 「 役職名(X) 」のパーツを作成して結合していく
                    val item = Component.text(" [${role.roleName}(${count})]", NamedTextColor.WHITE)
                        .decoration(TextDecoration.UNDERLINED, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick jobselect ${role.name}"))
                        .append(Component.text(" ", countColor))

                    rowComponent = rowComponent.append(item)
                }

                // 陣営ごとに1行でドンと送信（大幅な行数カット）
                sender.sendMessage(rowComponent)
            }

            sender.sendMessage(Component.text("============================", NamedTextColor.GOLD))
            1
        }
    }
}