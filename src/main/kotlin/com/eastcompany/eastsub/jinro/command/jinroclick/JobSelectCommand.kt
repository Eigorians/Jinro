package com.eastcompany.eastsub.jinro.command.jinroclick

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.command.jinro.SubCommand
import com.eastcompany.eastsub.jinro.game.Role
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class JobSelectCommand : SubCommand {
    override val name: String = "jobselect"
    private val plugin: Jinro get() = Jinro.instance
    private val sidebarFont = Key.key("minecraft:role_sidebar")

    override fun register(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(name)
            .then(Commands.argument("roleEnum", StringArgumentType.word())
                .executes { context ->
                    val sender = context.source.sender
                    val roleEnumStr = StringArgumentType.getString(context, "roleEnum")

                    val role = runCatching { Role.valueOf(roleEnumStr) }.getOrNull()
                    if (role == null) {
                        sender.sendMessage(Component.text("不正な役職名です。", NamedTextColor.RED))
                        return@executes 1
                    }

                    val config = plugin.configManager.gameConfig
                    val currentCount = config.registeredRoles[role] ?: 0

                    repeat(15) {
                        sender.sendMessage(Component.text(""))
                    }

                    sender.sendMessage(Component.text("\uF004\n", NamedTextColor.GRAY))
                    sender.sendMessage(Component.text("設定中: ${role.roleName} (${role.englishName})", NamedTextColor.YELLOW))

                    // 最大人数 < X人 > のUIを組み立て
                    val changeLine = Component.text("  最大人数 ")
                        .append(Component.text("<", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick jobchange ${role.name} down")))
                        .append(Component.text(" ${currentCount}人 ", NamedTextColor.WHITE))
                        .append(Component.text(">", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true)
                            .clickEvent(ClickEvent.runCommand("/jinroclick jobchange ${role.name} up")))

                    sender.sendMessage(changeLine)
                    sender.sendMessage(Component.text(""))

                    // ─── 💡 ポンコツ専用の追加UI ───
                    if (role == Role.PONKOTSU) {
                        sender.sendMessage(Component.text("  [ 化ける役職の設定 ]", NamedTextColor.AQUA))

                        // トグル対象にしたい役職（村人や人狼自身などを除いた、能力を持つ主要な役職）
                        val targetRoles = listOf(
                            Role.URANAI, Role.REINOU, Role.SHAMAN,
                            Role.SOUJIYA, Role.SAIBAN, Role.KISHI
                        )

                        // role.yml から現在のポンコツの選択可能リストを取得
                        val currentSelectables = plugin.roleConfigManager.roleData.ponkotsu.selectableRoles

                        var rowComponent = Component.text("  ")
                        targetRoles.forEach { target ->
                            val isSelected = currentSelectables.contains(target)

                            // 選択されているものはデフォルト色、選択されていないものはグレー
                            val effectColor = if (isSelected) null else NamedTextColor.GRAY

                            // 各役職アイコンを直に結合（クリックで専用のトグルコマンドを実行）
                            val item = Component.text()
                                .append(Component.text(target.character).font(sidebarFont).color(effectColor))
                                .clickEvent(ClickEvent.runCommand("/jinroclick ponkotsutoggle ${target.name}"))
                                .build()

                            rowComponent = rowComponent.append(item)
                        }
                        sender.sendMessage(rowComponent)
                    }
                    // ──────────────────────────────

                    // 戻るボタン
                    val backButton = Component.text()
                        .append(Component.text("\n\uF003"))
                        .clickEvent(ClickEvent.runCommand("/jinroclick job"))

                    sender.sendMessage(backButton)
                    1
                }
            )
    }
}