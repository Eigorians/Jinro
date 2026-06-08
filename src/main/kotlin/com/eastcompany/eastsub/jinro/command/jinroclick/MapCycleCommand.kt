package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit

class MapCycleCommand : SubCommand {
    override val name: String = "mapcycle" // /jinroclick mapcycle

    private val plugin: Jinro get() = Jinro.instance

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .executes { context ->
                val manager = plugin.configManager
                val config = manager.gameConfig
                val maps = config.mapList

                // マップリストが空でなければ次の要素へ切り替え
                if (maps.isNotEmpty()) {
                    val currentIndex = maps.indexOf(config.selectedMap)
                    // 次のインデックスを計算（末尾なら0に戻るループ構造）
                    val nextIndex = (currentIndex + 1) % maps.size
                    config.selectedMap = maps[nextIndex]

                    // Configに即時保存
                    manager.save()
                }

                // 「その他の設定」画面を再描画して最新のマップ名にする
                val server = Bukkit.getServer()
                server.dispatchCommand(context.source.sender, "jinroclick other")

                1
            }
    }
}