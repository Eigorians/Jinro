package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role

data class RoleConfig(
    // --- 既存の役職 ---
    val werewolf: WerewolfConfig,
    val assassin: AssassinConfig,
    val shuffler: ShufflerConfig,
    val trapper: TrapperConfig,
    val seer: SeerConfig,
    val medium: MediumConfig,

    // --- 追加の役職 ---
    val knight: KnightConfig,
    val cleaner: CleanerConfig,
    val ponkotsu: PonkotsuConfig,
    val judge: JudgeConfig,
    val magicalGirl: MagicalGirlConfig
)

// 既存の役職のデータクラス（省略せずに記述）
data class WerewolfConfig(val slashDays: Int, val slashMax: Int, val roarSeconds: Int, val roarMax: Int)
data class AssassinConfig(val slashDays: Int, val slashMax: Int)
data class ShufflerConfig(val slashDays: Int, val slashMax: Int, val shuffleSeconds: Int, val shuffleMax: Int)
data class TrapperConfig(val pitfallSeconds: Int, val pitfallMax: Int, val maxSimultaneous: Int)
data class SeerConfig(val cooldownDays: Int, val maxUses: Int)
data class MediumConfig(val cooldownSeconds: Int, val maxUses: Int)

// 新規追加のデータクラス
data class KnightConfig(val cooldownSeconds: Int, val maxUses: Int)
data class CleanerConfig(val cooldownDays: Int, val maxUses: Int)
data class PonkotsuConfig(
    val isWerewolfRole: Boolean,
    val selectableRoles: List<Role> // 変換済みのRoleリストを保持
)
data class JudgeConfig(val durationSeconds: Int, val skipHanging: Boolean, val vanishGraveAfterTrial: Boolean)
data class MagicalGirlConfig(val isDummyRole: Boolean)