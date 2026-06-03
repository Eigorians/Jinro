package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.game.Camp
import io.papermc.paper.scoreboard.numbers.NumberFormat
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective

object JinroScoreboardManager {
    private val plugin = JavaPlugin.getPlugin(Jinro::class.java)
    private val sidebarFont = Key.key("minecraft:role_sidebar")
    private const val OBJECTIVE_NAME = "jinro_recruit"

    /**
     * 右側のバニラ数字を消去し、Roleクラスの記載順のまま表示する
     */
    fun displayRecruitBoard() {
        val manager = Bukkit.getScoreboardManager()
        val board = manager.mainScoreboard
        val config = plugin.configManager.gameConfig
        val registeredRoles = config.registeredRoles

        // 重複エラー防止
        board.getObjective(OBJECTIVE_NAME)?.unregister()

        val objective: Objective = board.registerNewObjective(
            OBJECTIVE_NAME,
            Criteria.DUMMY,
            Component.text("役職", NamedTextColor.GOLD)
        )
        objective.displaySlot = DisplaySlot.SIDEBAR

        // ─── ✨ 修正：Role Enumの記載順（ordinal）にソート ───
        val activeRoles = Role.entries
            .filter { it != Role.BACKSLIDER }
            .filter { (registeredRoles[it] ?: 0) > 0 }
            .sortedBy { it.ordinal } // 💡 これでEnumの定義順（上から順）になります

        // Enumの上にある役職ほどスコアを高くして上に配置する
        var scoreIndex = activeRoles.size

        activeRoles.forEach { role ->
            val count = registeredRoles[role] ?: 0

            // 内部管理用のダミーエントリー名（重複防止）
            val entryName = "§r" + "§".ordering(scoreIndex)

            board.getTeam(role.name)?.unregister()

            val team = board.registerNewTeam(role.name).apply {
                addEntry(entryName)

                // プレフィックスに「役職アイコン + 陣営アイコン」をセット
                val icons = Component.text()
                    .append(Component.text(role.character).font(sidebarFont))
                    .append(Component.text(role.camp.icon))
                    .build()
                prefix(icons)

                // サフィックスに「 x人数」をセット
                suffix(Component.text(" x${count}", NamedTextColor.WHITE))
            }

            val score = objective.getScore(entryName)

            // 内部スコアを割り当てて並び順を固定
            score.score = scoreIndex

            // 右側のバニラ赤文字数字を完全消去
            score.numberFormat(NumberFormat.blank())

            scoreIndex-- // 下の行へ移動
        }
    }

    /**
     * メインスコアボードから人狼用の表示（Objective）とチームを完全に消去する
     */
    fun clearBoard() {
        val manager = Bukkit.getScoreboardManager()
        val board = manager.mainScoreboard

        board.getObjective(OBJECTIVE_NAME)?.unregister()

        Role.entries.forEach { role ->
            board.getTeam(role.name)?.unregister()
        }
    }

    private fun String.ordering(index: Int): String {
        return index.toString().map { "§$it" }.joinToString("")
    }
}