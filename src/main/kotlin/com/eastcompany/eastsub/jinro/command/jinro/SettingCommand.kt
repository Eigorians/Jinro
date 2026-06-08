package com.eastcompany.eastsub.jinro.command

import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor

class SettingCommand : SubCommand {
    override val name: String = "setting"

    // 実行ロジックを切り出し
    fun execute(sender: org.bukkit.command.CommandSender): Int {
        repeat(11) { sender.sendMessage(Component.text("")) }

        val titleText = Component.text("\uF002\n")
        sender.sendMessage("\n")

        val mapBtn = createClickableMenu("\uF020", "/jinro map", null)
        val jobBtn = createClickableMenu("\uF004", "/jinroclick job", null)
        val shopBtn = createClickableMenu("\uF005", "/jinroclick shop", null)
        val otherBtn = createClickableMenu("\uF006", "/jinroclick other", null)

        val controlRow = Component.text("  ")
            .append(Component.text("[開始]", NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand("/jinro start")))
            .append(Component.text(" / ", NamedTextColor.GRAY))
            .append(Component.text("[停止]", NamedTextColor.RED).clickEvent(ClickEvent.runCommand("/jinro stop")))

        sender.sendMessage(titleText)
        sender.sendMessage(mapBtn)
        sender.sendMessage(jobBtn)
        sender.sendMessage(shopBtn)
        sender.sendMessage(otherBtn)
        sender.sendMessage(Component.text(""))
        sender.sendMessage(controlRow)
        sender.sendMessage("")
        return 1
    }

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { execute(it.source.sender) } // ここでも呼び出し
    }

    private fun createClickableMenu(text: String, command: String, textColor: NamedTextColor?): Component {
        return Component.text(text, textColor).clickEvent(ClickEvent.runCommand(command))
    }
}