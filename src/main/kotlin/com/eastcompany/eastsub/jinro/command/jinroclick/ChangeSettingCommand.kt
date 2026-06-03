package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ChangeSettingCommand : SubCommand {
    override val name: String = "change"

    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        // メインのリテラル "change"
        val changeBase = Commands.literal(name)

        // 1. 既存の数値増減用ツリー: /jinroclick change [key] [action]
        val valueChangeTree = Commands.argument("key", StringArgumentType.word())
            .then(Commands.argument("action", StringArgumentType.word())
                .executes { context ->
                    val key = StringArgumentType.getString(context, "key")
                    val action = StringArgumentType.getString(context, "action")
                    val manager = plugin.configManager
                    val config = manager.gameConfig

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
                    }

                    manager.save()
                    refreshUI(context.source)
                    1
                }
            )

        // 2. マップ切り替え(ローテーション): /jinroclick change mapcycle
        val mapCycleTree = Commands.literal("mapcycle")
            .executes { context ->
                val manager = plugin.configManager
                val config = manager.gameConfig
                val maps = config.mapList

                if (maps.isNotEmpty()) {
                    // 現在のインデックスを取得して次のマップへシフト（見つからなければ最初へ）
                    val currentIndex = maps.indexOf(config.selectedMap)
                    val nextIndex = (currentIndex + 1) % maps.size
                    config.selectedMap = maps[nextIndex]

                    manager.save()
                }
                refreshUI(context.source)
                1
            }

        // 3. マップ新規作成: /jinroclick change mapcreate [マップ名]
        val mapCreateTree = Commands.literal("mapcreate")
            .then(Commands.argument("mapName", StringArgumentType.word())
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
                    config.selectedMap = mapName // 追加したマップを自動選択状態にする

                    manager.save()
                    refreshUI(context.source)
                    context.source.sender.sendMessage(Component.text("マップ「${mapName}」を作成しました。", NamedTextColor.GREEN))
                    1
                }
            )

        // 4. マップ削除: /jinroclick change mapdelete [マップ名]
        val mapDeleteTree = Commands.literal("mapdelete")
            .then(Commands.argument("mapName", StringArgumentType.word())
                .executes { context ->
                    val mapName = StringArgumentType.getString(context, "mapName")
                    val manager = plugin.configManager
                    val config = manager.gameConfig
                    val currentList = config.mapList.toMutableList()

                    if (!currentList.contains(mapName)) {
                        context.source.sender.sendMessage(Component.text("指定されたマップが存在しません。", NamedTextColor.RED))
                        return@executes 1
                    }

                    // 最後の1つは削除させないガード
                    if (currentList.size <= 1) {
                        context.source.sender.sendMessage(Component.text("これ以上マップを削除できません（最低1つのマップが必要です）。", NamedTextColor.RED))
                        return@executes 1
                    }

                    currentList.remove(mapName)
                    config.mapList = currentList

                    // もし現在選択中のマップを消した場合は、リストの最初の要素を自動的に再選択する
                    if (config.selectedMap == mapName) {
                        config.selectedMap = currentList.first()
                    }

                    manager.save()
                    refreshUI(context.source)
                    context.source.sender.sendMessage(Component.text("マップ「${mapName}」を削除しました。", NamedTextColor.YELLOW))
                    1
                }
            )

        // すべてのサブツリーを親コマンド "change" にバインド
        return changeBase
            .then(valueChangeTree)
            .then(mapCycleTree)
            .then(mapCreateTree)
            .then(mapDeleteTree)
    }

    /**
     * 安全に設定UIをチャット欄へ再描写する共通関数
     */
    private fun refreshUI(source: CommandSourceStack) {
        val server = Bukkit.getServer()
        server.dispatchCommand(source.sender, "jinroclick other")
    }
}