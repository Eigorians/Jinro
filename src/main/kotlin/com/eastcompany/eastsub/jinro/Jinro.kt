package com.eastcompany.eastsub.jinro

import com.eastcompany.eastsub.jinro.command.jinro.JinroCommand
import com.eastcompany.eastsub.jinro.command.jinroclick.JinroClickCommand
import com.eastcompany.eastsub.jinro.config.JinroConfigManager
import com.eastcompany.eastsub.jinro.listener.MapToolListener
import com.eastcompany.eastsub.jinro.manager.JinroGameManager    // ✨ 追加
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager   // ✨ 追加
import com.eastcompany.eastsub.jinro.manager.JinroScoreboardManager
import com.eastcompany.eastsub.jinro.listener.task.ToolParticleTask
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Jinro : JavaPlugin() {

    lateinit var configManager: JinroConfigManager
        private set

    lateinit var toolParticleTask: ToolParticleTask
        private set

    override fun onEnable() {
        // 先にConfigManagerを初期化
        configManager = JinroConfigManager(this)

        val manager = this.lifecycleManager

        // コマンド登録のイベントハンドラ
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

        // 先にタスクをインスタンス化
        toolParticleTask = ToolParticleTask(this)
        // 5ティック（0.25秒）毎にパーティクル表示をスキャン実行
        toolParticleTask.runTaskTimer(this, 0L, 5L)

        // リスナー関係の登録
        server.pluginManager.registerEvents(MapToolListener(this), this)
    }

    override fun onDisable() {
        // 💡 ✨【常時ストップ対策】プラグインが無効化されるときは、ゲームが募集中でも本番中でも常に完全停止させる
        // 各reset()の内部でカウントダウンタスク、BossBar、本編TimeManagerタイマーが漏れなく消滅します
        JinroMatchManager.reset()
        JinroGameManager.reset()

        // サーバー停止/リロード時に未消滅のプレビューエンティティを確実に全消去する
        if (::toolParticleTask.isInitialized) {
            toolParticleTask.clearAll()
        }

        // スコアボードの完全クリア
        JinroScoreboardManager.clearBoard()
    }

}