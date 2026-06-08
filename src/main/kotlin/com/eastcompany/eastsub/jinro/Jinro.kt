package com.eastcompany.eastsub.jinro

import com.eastcompany.eastsub.jinro.command.jinro.JinroCommand
import com.eastcompany.eastsub.jinro.command.jinroclick.JinroClickCommand
import com.eastcompany.eastsub.jinro.config.JinroConfigManager
import com.eastcompany.eastsub.jinro.config.RoleConfigManager
import com.eastcompany.eastsub.jinro.item.job.SpecialItemManager
import com.eastcompany.eastsub.jinro.item.shop.CustomItemListener
import com.eastcompany.eastsub.jinro.listener.MapToolListener
import com.eastcompany.eastsub.jinro.listener.task.ToolParticleTask
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import com.eastcompany.eastsub.jinro.manager.JinroMatchManager
import com.eastcompany.eastsub.jinro.manager.JinroScoreboardManager
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class Jinro : JavaPlugin() {

    lateinit var configManager: JinroConfigManager
        private set

    lateinit var toolParticleTask: ToolParticleTask
        private set

    lateinit var itemManager: SpecialItemManager
        private set

    companion object {
        lateinit var instance: Jinro
            private set
    }

    lateinit var roleConfigManager: RoleConfigManager
        private set

    override fun onEnable() {
        // 先にConfigManagerを初期化
        instance = this

        configManager = JinroConfigManager(this)

        val manager = this.lifecycleManager
        roleConfigManager = RoleConfigManager(this)
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

        server.pluginManager.registerEvents(CustomItemListener(), this)

        itemManager = SpecialItemManager(this)
        server.pluginManager.registerEvents(itemManager, this)
    }

    override fun onDisable() {
        // 💡 ✨【常時ストップ対策】プラグインが無効化されるときは、ゲームが募集中でも本番中でも常に完全停止させる
        // 各reset()の内部でカウントダウンタスク、BossBar、本編TimeManagerタイマーが漏れなく消滅します
        JinroMatchManager.reset()
        JinroGameManager.reset()

        // サーバー停止/リロード時に未消滅のプレビューエンティティを確実に全消去する
        if (::toolParticleTask.isInitialized) {
            toolParticleTask.cancel()
            // 2. その後に全削除
            toolParticleTask.clearAll()
        }

        // スコアボードの完全クリア
        JinroScoreboardManager.clearBoard()
    }

}