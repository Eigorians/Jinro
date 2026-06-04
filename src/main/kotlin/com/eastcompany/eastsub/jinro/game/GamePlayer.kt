package com.eastcompany.eastsub.jinro.game

import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.attribute.Attribute
import java.util.UUID

data class GamePlayer(
    // プレイヤー本体
    val offlinePlayer: OfflinePlayer,

    // 役職
    var role: Role,

    // 現在の陣営（デフォルトは役職の陣営）
    var camp: Camp = role.camp,

    // 生死状態
    var isAlive: Boolean = true,

    // 特殊能力の使用制限フラグ
    var abilityStatus: AbilityStatus = AbilityStatus.AVAILABLE,

    // 恋人フラグ（追加！）
    var isLovers: Boolean = false,

    //裁判官後任フラグ
    var isSaiban: Boolean = false
) {
    // 便利なショートカットプロパティ
    val uuid: UUID = offlinePlayer.uniqueId
    val name: String = offlinePlayer.name ?: "Unknown"

    /**
     * ✨ プレイヤーのステータスを完全リセットする
     * （インベントリ消去、体力・満腹度全回復、ゲームモードをアドベンチャーに変更）
     */
    fun resetState() {
        // オンライン中のプレイヤー実体を取得（オフラインならスキップ）
        val player = offlinePlayer.player ?: return

        // 1. インベントリと装備の完全クリア
        player.inventory.clear()

        // 2. 体力の回復（属性から最大体力を取得して安全に全回復）
        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.health = maxHealth

        // 3. 満腹度と隠し満腹度（サチュレーション）のリセット
        player.foodLevel = 20
        player.saturation = 5.0f

        // 4. アクティブなポーションエフェクトをすべて消去
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }

        // 5. ゲームモードをアドベンチャー（ADVENTURE）に変更
        player.gameMode = GameMode.ADVENTURE
    }
}