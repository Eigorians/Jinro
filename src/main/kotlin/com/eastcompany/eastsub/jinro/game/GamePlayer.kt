package com.eastcompany.eastsub.jinro.game

import org.bukkit.OfflinePlayer
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
}