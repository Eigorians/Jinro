package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class RoleConfigManager(private val plugin: JavaPlugin) {

    companion object {
        // --- 既存のデフォルト値 ---
        const val DEFAULT_WEREWOLF_SLASH_DAYS = 1
        const val DEFAULT_WEREWOLF_SLASH_MAX = 1
        const val DEFAULT_WEREWOLF_ROAR_SECONDS = 30
        const val DEFAULT_WEREWOLF_ROAR_MAX = 1

        const val DEFAULT_ASSASSIN_SLASH_DAYS = 1
        const val DEFAULT_ASSASSIN_SLASH_MAX = 1

        const val DEFAULT_SHUFFLER_SLASH_DAYS = 1
        const val DEFAULT_SHUFFLER_SLASH_MAX = 1
        const val DEFAULT_SHUFFLER_SHUFFLE_SECONDS = 120
        const val DEFAULT_SHUFFLER_SHUFFLE_MAX = 1

        const val DEFAULT_TRAPPER_PITFALL_SECONDS = 30
        const val DEFAULT_TRAPPER_PITFALL_MAX = 1
        const val DEFAULT_TRAPPER_MAX_SIMULTANEOUS = 4

        const val DEFAULT_SEER_COOLDOWN_DAYS = 2
        const val DEFAULT_SEER_MAX_USES = 1

        const val DEFAULT_MEDIUM_COOLDOWN_SECONDS = 1
        const val DEFAULT_MEDIUM_MAX_USES = 1

        // --- 新規追加のデフォルト値 ---
        // 騎士 (Knight)
        const val DEFAULT_KNIGHT_COOLDOWN_SECONDS = 1
        const val DEFAULT_KNIGHT_MAX_USES = 1

        // 掃除屋 (Cleaner)
        const val DEFAULT_CLEANER_COOLDOWN_DAYS = 1
        const val DEFAULT_CLEANER_MAX_USES = 1

        // ポンコツ (Ponkotsu)
        const val DEFAULT_PONKOTSU_IS_WEREWOLF_ROLE = true
        // 💡 追加: 外部から参照できる「ポンコツが選ばれる職業」のデフォルトリスト
        val DEFAULT_PONKOTSU_SELECTABLE_ROLES = listOf(
            Role.URANAI,
            Role.REINOU,
            Role.SHAMAN,
            Role.SOUJIYA,
            Role.SAIBAN,
            Role.KISHI
        )

        // 裁判官 (Judge)
        const val DEFAULT_JUDGE_DURATION_SECONDS = 120
        const val DEFAULT_JUDGE_SKIP_HANGING = true
        const val DEFAULT_JUDGE_VANISH_GRAVE_AFTER_TRIAL = false

        // 魔法少女 (Magical Girl)
        const val DEFAULT_MAGICAL_GIRL_IS_DUMMY_ROLE = false
    }

    private val configFile = File(plugin.dataFolder, "role.yml")
    private lateinit var config: YamlConfiguration

    lateinit var roleData: RoleConfig
        private set

    init {
        loadConfig()
    }

    fun loadConfig() {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }

        if (!configFile.exists()) {
            plugin.saveResource("role.yml", false)
        }

        config = YamlConfiguration.loadConfiguration(configFile)
        mapToClass()
    }

    private fun mapToClass() {
        // 💡 ポンコツの選ばれる職業リストのロードと安全な型変換処理
        val rawRoleStrings = config.getStringList("ponkotsu.selectable_roles")
        val loadedRoles = if (rawRoleStrings.isEmpty()) {
            DEFAULT_PONKOTSU_SELECTABLE_ROLES
        } else {
            rawRoleStrings.mapNotNull { name ->
                runCatching { Role.valueOf(name.uppercase()) }.getOrNull()
            }
        }

        roleData = RoleConfig(
            // 既存の役職マッピング
            werewolf = WerewolfConfig(
                slashDays = config.getInt("werewolf.slash.cooldown_days", DEFAULT_WEREWOLF_SLASH_DAYS),
                slashMax = config.getInt("werewolf.slash.max_uses", DEFAULT_WEREWOLF_SLASH_MAX),
                roarSeconds = config.getInt("werewolf.roar.cooldown_seconds", DEFAULT_WEREWOLF_ROAR_SECONDS),
                roarMax = config.getInt("werewolf.roar.max_uses", DEFAULT_WEREWOLF_ROAR_MAX)
            ),
            assassin = AssassinConfig(
                slashDays = config.getInt("assassin.slash.cooldown_days", DEFAULT_ASSASSIN_SLASH_DAYS),
                slashMax = config.getInt("assassin.slash.max_uses", DEFAULT_ASSASSIN_SLASH_MAX)
            ),
            shuffler = ShufflerConfig(
                slashDays = config.getInt("shuffler.slash.cooldown_days", DEFAULT_SHUFFLER_SLASH_DAYS),
                slashMax = config.getInt("shuffler.slash.max_uses", DEFAULT_SHUFFLER_SLASH_MAX),
                shuffleSeconds = config.getInt("shuffler.shuffle.cooldown_seconds", DEFAULT_SHUFFLER_SHUFFLE_SECONDS),
                shuffleMax = config.getInt("shuffler.shuffle.max_uses", DEFAULT_SHUFFLER_SHUFFLE_MAX)
            ),
            trapper = TrapperConfig(
                pitfallSeconds = config.getInt("trapper.pitfall.cooldown_seconds", DEFAULT_TRAPPER_PITFALL_SECONDS),
                pitfallMax = config.getInt("trapper.pitfall.max_uses", DEFAULT_TRAPPER_PITFALL_MAX),
                maxSimultaneous = config.getInt("trapper.pitfall.max_simultaneous", DEFAULT_TRAPPER_MAX_SIMULTANEOUS)
            ),
            seer = SeerConfig(
                cooldownDays = config.getInt("seer.divination.cooldown_days", DEFAULT_SEER_COOLDOWN_DAYS),
                maxUses = config.getInt("seer.divination.max_uses", DEFAULT_SEER_MAX_USES)
            ),
            medium = MediumConfig(
                cooldownSeconds = config.getInt("medium.necromancy.cooldown_seconds", DEFAULT_MEDIUM_COOLDOWN_SECONDS),
                maxUses = config.getInt("medium.necromancy.max_uses", DEFAULT_MEDIUM_MAX_USES)
            ),

            // 新規追加の役職マッピング
            knight = KnightConfig(
                cooldownSeconds = config.getInt("knight.protection.cooldown_seconds", DEFAULT_KNIGHT_COOLDOWN_SECONDS),
                maxUses = config.getInt("knight.protection.max_uses", DEFAULT_KNIGHT_MAX_USES)
            ),
            cleaner = CleanerConfig(
                cooldownDays = config.getInt("cleaner.cleanup.cooldown_days", DEFAULT_CLEANER_COOLDOWN_DAYS),
                maxUses = config.getInt("cleaner.cleanup.max_uses", DEFAULT_CLEANER_MAX_USES)
            ),
            // 💡 修正: 変換したリスト（loadedRoles）をデータクラスに渡す
            ponkotsu = PonkotsuConfig(
                isWerewolfRole = config.getBoolean("ponkotsu.is_werewolf_role", DEFAULT_PONKOTSU_IS_WEREWOLF_ROLE),
                selectableRoles = loadedRoles
            ),
            judge = JudgeConfig(
                durationSeconds = config.getInt("judge.trial.duration_seconds", DEFAULT_JUDGE_DURATION_SECONDS),
                skipHanging = config.getBoolean("judge.trial.skip_hanging", DEFAULT_JUDGE_SKIP_HANGING),
                vanishGraveAfterTrial = config.getBoolean("judge.trial.vanish_grave_after_trial", DEFAULT_JUDGE_VANISH_GRAVE_AFTER_TRIAL)
            ),
            magicalGirl = MagicalGirlConfig(
                isDummyRole = config.getBoolean("magical_girl.is_dummy_role", DEFAULT_MAGICAL_GIRL_IS_DUMMY_ROLE)
            )
        )
    }
}