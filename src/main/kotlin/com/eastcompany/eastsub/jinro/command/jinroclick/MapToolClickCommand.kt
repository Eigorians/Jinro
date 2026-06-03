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
import org.bukkit.plugin.java.JavaPlugin

class MapToolClickCommand : SubCommand {
    override val name: String = "maptool"
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { context ->
            val sender = context.source.sender
            if (sender !is Player) return@executes 1

            val currentMap = plugin.configManager.gameConfig.selectedMap

            sender.sendMessage(Component.text("「${currentMap}」用のツール一式を付与しました。", NamedTextColor.GREEN))

            // 6つのツールを付与
            sender.inventory.addItem(createTool(Material.WHITE_WOOL, "ロビー登録ツール", currentMap, "右クリック: ロビー位置を登録 (1箇所)"))
            sender.inventory.addItem(createTool(Material.BLAZE_ROD, "スポーン登録ツール", currentMap, "右クリック: スポーン位置を追加 / 左クリック: 2マス以内を削除"))
            sender.inventory.addItem(createTool(Material.EMERALD, "ショップ登録ツール", currentMap, "右クリック: ショップ位置を追加 / 左クリック: 2マス以内を削除"))
            sender.inventory.addItem(createTool(Material.IRON_SWORD, "裁判所登録ツール", currentMap, "右クリック: 裁判所位置を登録 (1箇所)"))
            sender.inventory.addItem(createTool(Material.BONE, "地下範囲指定ツール", currentMap, "右クリック: 地下のY座標基準位置を指定"))
            sender.inventory.addItem(createTool(Material.IRON_PICKAXE, "復活資源登録ツール", currentMap, "右クリック: 設置位置指定 / ブロック破壊: 座標指定"))

            1
        }
    }

    private fun createTool(material: Material, name: String, mapName: String, description: String): ItemStack {
        return ItemStack(material).apply {
            editMeta { meta ->
                meta.displayName(Component.text(name, NamedTextColor.AQUA))
                meta.lore(listOf(
                    Component.text("対象マップ: $mapName", NamedTextColor.YELLOW),
                    Component.text(description, NamedTextColor.GRAY)
                ))
                // ⚠️ マップ名を外部から改ざんされないようメタデータのコンテナ内にNBTとして保存
                val key = NamespacedKey(plugin, "tool_map_name")
                meta.persistentDataContainer.set(key, PersistentDataType.STRING, mapName)
            }
        }
    }
}