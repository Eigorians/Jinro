package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class MapChangeClickCommand : SubCommand {
    override val name: String = "mapchange"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("action", StringArgumentType.word())
                .executes { context ->
                    val action = StringArgumentType.getString(context, "action")
                    val manager = plugin.configManager
                    val config = manager.gameConfig
                    val maps = config.mapList

                    if (maps.isNotEmpty()) {
                        val currentIndex = maps.indexOf(config.selectedMap)
                        var nextIndex = currentIndex

                        if (action == "up") {
                            nextIndex = (currentIndex + 1) % maps.size
                        } else if (action == "down") {
                            nextIndex = (currentIndex - 1 + maps.size) % maps.size
                        }

                        config.selectedMap = maps[nextIndex]
                        manager.save()
                    }

                    // /jinro map を再実行してUIをリフレッシュ
                    val server = Bukkit.getServer()
                    server.dispatchCommand(context.source.sender, "jinro map")
                    1
                }
            )
    }
}