package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.plugin.java.JavaPlugin

class JobSettingCommand : SubCommand {
    override val name: String = "job"
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)
    private val sidebarFont = Key.key("minecraft:role_sidebar")

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name).executes { context ->
            val sender = context.source.sender
            val registeredRoles = plugin.configManager.gameConfig.registeredRoles

            repeat(10) {
                sender.sendMessage(Component.text(""))
            }

            sender.sendMessage(Component.text("\uF004\n"))

            // ─── グループ定義（背徳者と村人を除外） ───
            val selectableRoles = Role.entries.filter { it != Role.BACKSLIDER && it != Role.VILLAGER }

            val groups = listOf(
                GroupData(Camp.VILLAGER, "村人陣営", NamedTextColor.GREEN, selectableRoles.filter { it.camp == Camp.VILLAGER }),
                GroupData(Camp.WEREWOLF, "人狼陣営", NamedTextColor.RED, selectableRoles.filter { it.camp == Camp.WEREWOLF || it.camp == Camp.MADMAN }),
                GroupData(Camp.GRIM_REAPER, "第三陣営", NamedTextColor.LIGHT_PURPLE, selectableRoles.filter { it.camp != Camp.VILLAGER && it.camp != Camp.WEREWOLF && it.camp != Camp.MADMAN })
            )

            // ─── グループごとに描画 ───
            groups.forEach { group ->
                if (group.roles.isEmpty()) return@forEach

                // 💡 ✨ 【変更】陣営アイコンは無色（元の画像色）にし、テキスト部分だけに陣営カラーを適用
                val groupTitle = Component.text()
                    .append(Component.text(group.camp.icon)) // 無色
                    .append(Component.text(group.titleText, group.textColor))
                    .build()

                sender.sendMessage(groupTitle)

                var rowComponent = Component.text("")
                var countInRow = 0

                group.roles.forEach { role ->
                    val count = registeredRoles[role] ?: 0

                    // 0人の時はグレー、1人以上の時はデフォルト（画像本来の色）
                    val effectColor = if (count == 0) NamedTextColor.GRAY else null

                    // 💡 ✨ 【変更】後ろのスペース「    」を完全削除して直に結合
                    val item = Component.text()
                        .append(Component.text(role.character).font(sidebarFont).color(effectColor))
                        .clickEvent(ClickEvent.runCommand("/jinroclick jobselect ${role.name}"))

                    rowComponent = rowComponent.append(item)
                    countInRow++

                    // 横に5つ並んだら改行
                    if (countInRow == 5) {
                        sender.sendMessage(rowComponent)
                        rowComponent = Component.text("")
                        countInRow = 0
                    }
                }

                // 余った役職があれば最後に送信
                if (countInRow > 0) {
                    sender.sendMessage(rowComponent)
                }

            }

            // ─── 戻るボタンの追加 ───
            val backButton = Component.text()
                .append(Component.text("\n\uF003"))
                .clickEvent(ClickEvent.runCommand("/jinro setting"))

            sender.sendMessage(backButton)
            1
        }
    }

    private data class GroupData(
        val camp: Camp,
        val titleText: String,
        val textColor: NamedTextColor,
        val roles: List<Role>
    )
}