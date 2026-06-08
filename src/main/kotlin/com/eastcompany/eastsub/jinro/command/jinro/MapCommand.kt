package com.eastcompany.eastsub.jinro.command.jinro

import com.eastcompany.eastsub.jinro.Jinro
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class MapCommand : SubCommand {
    override val name: String = "map"

    private val plugin: Jinro get() = Jinro.instance

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        val mapBase = Commands.literal(name)
            // ⚠️ 引数なしで /jinro map と打たれた時にチャットUIを表示
            .executes { context ->
                val sender = context.source.sender
                val config = plugin.configManager.gameConfig

                repeat(15) {
                    sender.sendMessage(Component.text(""))
                }

                sender.sendMessage(Component.text("\uF020\n"))

                // マップ選択用の行： [<] [マップ名] [>]
                val selectLine = Component.text("  マップ選択: ")
                    .append(Component.text("<", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick mapchange down")))
                    .append(Component.text(" ${config.selectedMap} ", NamedTextColor.WHITE))
                    .append(Component.text(">", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                        .clickEvent(ClickEvent.runCommand("/jinroclick mapchange up")))

                // ツール入手ボタンの行
                val toolLine = Component.text("  [ マップ作成ツールを入手 ]", NamedTextColor.AQUA)
                    .decoration(TextDecoration.UNDERLINED, true)
                    .clickEvent(ClickEvent.runCommand("/jinroclick maptool"))

                sender.sendMessage(selectLine)
                sender.sendMessage(Component.text("")) // 空行
                sender.sendMessage(toolLine)
                val backButton = Component.text()
                    .append(Component.text("\n\uF003"))
                    .clickEvent(ClickEvent.runCommand("/jinro setting"))

                sender.sendMessage(backButton)
                1
            }

        // /jinro map create [マップ名]
        val createSub = Commands.literal("create")
            .then(Commands.argument("mapName", StringArgumentType.greedyString())
                .executes { context ->
                    val mapName = StringArgumentType.getString(context, "mapName")
                    val manager = plugin.configManager
                    val config = manager.gameConfig
                    val currentList = config.mapList.toMutableList()

                    if (currentList.contains(mapName)) {
                        context.source.sender.sendMessage(Component.text("そのマップ名は既に存在します。", NamedTextColor.RED))
                        return@executes 1
                    }

                    currentList.add(mapName)
                    config.mapList = currentList
                    config.selectedMap = mapName

                    manager.save()
                    context.source.sender.sendMessage(Component.text("マップ「${mapName}」を作成し、選択しました。", NamedTextColor.GREEN))
                    1
                }
            )

        // /jinro map delete [マップ名]
        val deleteSub = Commands.literal("delete")
            .then(Commands.argument("mapName", StringArgumentType.greedyString())
                // === ここを追加：タブ補完の予測変換を設定 ===
                .suggests { context, builder ->
                    val config = plugin.configManager.gameConfig

                    // 現在のmapListに登録されている文字列をループで候補に追加
                    for (mapName in config.mapList) {
                        // 入力中の文字と前方一致するものだけを出す（Brigadierの標準挙動に合わせる）
                        if (mapName.startsWith(builder.remaining, ignoreCase = true)) {
                            builder.suggest(mapName)
                        }
                    }
                    builder.buildFuture() // 補完候補を確定させて返す
                }
                // ===========================================
                .executes { context ->
                    val mapName = StringArgumentType.getString(context, "mapName").trim()

                    val manager = plugin.configManager
                    val config = manager.gameConfig
                    val currentList = config.mapList.toMutableList()

                    if (!currentList.contains(mapName)) {
                        context.source.sender.sendMessage(Component.text("指定されたマップが存在しません。", NamedTextColor.RED))
                        return@executes 1
                    }

                    if (currentList.size <= 1) {
                        context.source.sender.sendMessage(Component.text("最低1つのマップが必要です。", NamedTextColor.RED))
                        return@executes 1
                    }

                    currentList.remove(mapName)
                    config.mapList = currentList

                    if (config.selectedMap == mapName) {
                        config.selectedMap = currentList.first()
                    }

                    manager.save()
                    context.source.sender.sendMessage(Component.text("マップ「${mapName}」を削除しました。", NamedTextColor.YELLOW))
                    1
                }
            )

        return mapBase.then(createSub).then(deleteSub)
    }
}