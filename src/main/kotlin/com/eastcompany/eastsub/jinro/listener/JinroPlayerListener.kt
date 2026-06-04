package com.eastcompany.eastsub.jinro.listener

import com.eastcompany.eastsub.jinro.manager.JinroTimeManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JinroPlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        // 💡 現在ゲームのタイマー（BossBar）が動いている場合、再ログインしたプレイヤーをバーに追加する
        JinroTimeManager.getBossBar()?.let { bar ->
            // 重複して追加しようとしても内部で安全に無視されます
            bar.addPlayer(player)
        }
    }
}