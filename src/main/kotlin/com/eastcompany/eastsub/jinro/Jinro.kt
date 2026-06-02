package com.eastcompany.eastsub.jinro

import com.eastcompany.eastsub.jinro.command.jinro.JinroCommand
import com.eastcompany.eastsub.jinro.command.jinroclick.JinroClickCommand
import com.eastcompany.eastsub.jinro.config.JinroConfigManager
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Jinro : JavaPlugin() {

    lateinit var configManager: JinroConfigManager
        private set

    override fun onEnable() {
        // 先にConfigManagerを初期化
        configManager = JinroConfigManager(this)

        val manager = this.lifecycleManager

        // コマンド登録のイベントハンドラ（1つに集約）
        manager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val commands: Commands = event.registrar()

            // 1. /jinro コマンドの登録
            commands.register(
                JinroCommand.create().build(),
                "人狼ゲームのメインコマンド"
            )

            // 2. /jinroclick コマンドの登録
            commands.register(
                JinroClickCommand.create().build(),
                "UIクリックイベント処理用コマンド"
            )
        }
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}