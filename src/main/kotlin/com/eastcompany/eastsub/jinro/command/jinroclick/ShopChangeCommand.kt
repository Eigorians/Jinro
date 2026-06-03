package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class ShopChangeCommand : SubCommand {
    override val name: String = "shopchange"
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("itemKey", StringArgumentType.word())
                .then(Commands.argument("action", StringArgumentType.word())
                    .executes { context ->
                        val itemKey = StringArgumentType.getString(context, "itemKey")
                        val action = StringArgumentType.getString(context, "action")

                        val manager = plugin.configManager
                        val config = manager.gameConfig

                        val currentMap = config.shopPrices.toMutableMap()
                        val currentPrice = currentMap[itemKey] ?: 0

                        // 価格の増減ロジック（0G以下にはならないようにガード）
                        if (action == "up") {
                            currentMap[itemKey] = currentPrice + 1
                        } else if (action == "down" && currentPrice > 0) {
                            currentMap[itemKey] = currentPrice - 1
                        }

                        config.shopPrices = currentMap
                        manager.save()

                        // 💡 【ヒント】再描画先（"jinroclick shop" コマンド側）で色分けするために、
                        // ここで判定用のロジックを整理、またはショップ表示側のクラスで以下の判定を行います。
                        //
                        // val defaultPrice = config.defaultShopPrices[itemKey] ?: 0 (Configにデフォルト値のMapがあると仮定)
                        // val priceColor = when {
                        //     currentPrice == defaultPrice -> NamedTextColor.WHITE       // デフォルト値なら白
                        //     currentPrice < defaultPrice  -> NamedTextColor.LIGHT_PURPLE // それより低いならピンク (LIGHT_PURPLE)
                        //     else                         -> NamedTextColor.GREEN        // 高いなら薄緑 (GREEN)
                        // }

                        // 画面を再描画して数値を即時反映
                        val server = Bukkit.getServer()
                        server.dispatchCommand(context.source.sender, "jinroclick shop")

                        1
                    }
                )
            )
    }
}