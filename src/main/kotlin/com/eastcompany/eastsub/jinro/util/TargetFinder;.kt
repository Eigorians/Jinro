package com.eastcompany.eastsub.jinro.util

import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

/**
 * プレイヤーの視線からインタラクト対象を特定するクラス
 */
class TargetFinder(private val player: Player, private val maxDistance: Double = 4.4) {

    /**
     * 視線の先にあるターゲット（GamePlayer、Entity、またはNothing）を検索する
     */
    fun findTarget(): ClickTarget {
        val rayTraceResult = player.world.rayTraceEntities(
            player.eyeLocation,
            player.location.direction,
            maxDistance
        ) { entity -> entity != player } // 自分自身を除外

        val hitEntity = rayTraceResult?.hitEntity ?: return ClickTarget.Nothing

        // ターゲットがプレイヤーの場合、人狼ゲームの生存プレイヤーかチェック
        if (hitEntity is Player) {
            val targetGamePlayer = JinroGameManager.gamePlayers[hitEntity.uniqueId]
            if (targetGamePlayer != null && targetGamePlayer.isAlive) {
                return ClickTarget.GamePlayerTarget(targetGamePlayer)
            }
        }

        // それ以外のMobやゲーム外プレイヤーの場合
        return ClickTarget.EntityTarget(hitEntity)
    }
}

/**
 * ターゲットの種類を表すシーケンス（密封）クラス
 */
sealed class ClickTarget {
    class GamePlayerTarget(val gamePlayer: GamePlayer) : ClickTarget()
    class EntityTarget(val entity: Entity) : ClickTarget()
    object Nothing : ClickTarget()
}