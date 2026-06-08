package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MapToolClickCommand : SubCommand {
    override val name: String = "maptool"
    private val plugin: Jinro get() = Jinro.instance

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { context ->
            val sender = context.source.sender
            if (sender !is Player) return@executes 1

            val currentMap = plugin.configManager.gameConfig.selectedMap

            sender.sendMessage(Component.text("「${currentMap}」用のツール一式を付与しました。", NamedTextColor.GREEN))

            // 基本ツール
            // --- register() 内の付与部分 ---
            sender.inventory.addItem(createTool(Material.WHITE_WOOL, "ロビー登録ツール", currentMap, "右クリック: ロビー位置を登録 (1箇所)"))
            sender.inventory.addItem(createTool(Material.BLAZE_ROD, "スポーン登録ツール", currentMap, "右クリック: 追加 / 左クリック: 2マス以内削除"))
            sender.inventory.addItem(createTool(Material.EMERALD, "ショップ登録ツール", currentMap, "右クリック: 追加 / 左クリック: 2マス以内削除"))
            sender.inventory.addItem(createTool(Material.IRON_SWORD, "裁判所登録ツール", currentMap, "右クリック: ロビー位置を登録 (1箇所)"))
            sender.inventory.addItem(createTool(Material.BONE, "地下範囲指定ツール", currentMap, "右クリック: 地下のY座標基準位置を指定"))

// 💡 鉄ピッケルをやめ、見た目そのもののブロックアイテムに変更！
            sender.inventory.addItem(createTool(Material.POPPY, "資源登録ツール [花]", currentMap, "右クリック: 設置 / 左クリック: 2マス以内削除"))
            sender.inventory.addItem(createTool(Material.CHEST, "資源登録ツール [チェスト]", currentMap, "右クリック: 設置 / 左クリック: 2マス以内削除"))
            sender.inventory.addItem(createTool(Material.IRON_ORE, "資源登録ツール [鉄鉱石]", currentMap, "右クリック: 設置 / 左クリック: 2マス以内削除"))
            sender.inventory.addItem(createTool(Material.OAK_LOG, "資源登録ツール [木]", currentMap, "右クリック: 設置 / 左クリック: 2マス以内削除"))

            1
        }
    }

    // 💡 resourceType 引数を追加 (デフォルトは null)
    private fun createTool(material: Material, name: String, mapName: String, description: String, resourceType: String? = null): ItemStack {
        return ItemStack(material).apply {
            editMeta { meta ->
                meta.displayName(Component.text(name, NamedTextColor.AQUA))
                meta.lore(listOf(
                    Component.text("対象マップ: $mapName", NamedTextColor.YELLOW),
                    Component.text(description, NamedTextColor.GRAY)
                ))

                // マップ名保存用NBT
                val mapKey = NamespacedKey(plugin, "tool_map_name")
                meta.persistentDataContainer.set(mapKey, PersistentDataType.STRING, mapName)

                // 💡 資源タイプ保存用NBT（ピッケルのみ使用）
                if (resourceType != null) {
                    val resourceKey = NamespacedKey(plugin, "tool_resource_type")
                    meta.persistentDataContainer.set(resourceKey, PersistentDataType.STRING, resourceType)
                }
            }
        }
    }
}