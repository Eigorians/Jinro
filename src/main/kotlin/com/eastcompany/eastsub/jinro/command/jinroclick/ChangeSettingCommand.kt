package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.plugin.java.JavaPlugin

class ChangeSettingCommand : SubCommand {
    override val name: String = "change"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("key", StringArgumentType.word())
                .then(Commands.argument("action", StringArgumentType.word())
                    .executes { context ->
                        val key = StringArgumentType.getString(context, "key")
                        val action = StringArgumentType.getString(context, "action")
                        val manager = plugin.configManager
                        val config = manager.gameConfig

                        // 各設定の増減ロジック
                        when (key) {
                            "firstdaytime" -> {
                                if (action == "up") config.firstDayTime += 10
                                if (action == "down" && config.firstDayTime > 10) config.firstDayTime -= 10
                            }
                            "daytime" -> {
                                if (action == "up") config.dayTime += 10
                                if (action == "down" && config.dayTime > 10) config.dayTime -= 10
                            }
                            "nighttime" -> {
                                if (action == "up") config.nightTime += 10
                                if (action == "down" && config.nightTime > 10) config.nightTime -= 10
                            }
                            "werewolf" -> {
                                if (action == "up") config.fixedWerewolfCount += 1
                                if (action == "down" && config.fixedWerewolfCount > 0) config.fixedWerewolfCount -= 1
                            }
                            "field" -> {
                                config.fieldType = when (config.fieldType) {
                                    "default" -> "island"
                                    "island" -> "desert"
                                    else -> "default"
                                }
                            }
                        }

                        // 1. 変更されたデータをconfig.ymlに即時保存
                        manager.save()

                        // 2. 他のコマンドクラスをインスタンス化して、直接その処理（executesの中身）を呼び出す
                        // これにより performCommand なしで安全に再描画が可能です
                        val otherCommand = OtherSettingCommand()

                        // 他のコマンドのregisterツリーから直接実行ロジックを呼び出すか、
                        // もしくは、OtherSettingCommandに描画関数を作ってそれを呼ぶのが一番安全です。
                        // 今回は一番シンプルな「修正方法A」の dispatchCommand に頼るか、
                        // 以下の方法で直接 context を渡して実行します。

                        val server = org.bukkit.Bukkit.getServer()
                        server.dispatchCommand(context.source.sender, "jinroclick other")

                        1
                    }
                )
            )
    }
}