package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role
import org.bukkit.plugin.java.JavaPlugin

class JinroConfigManager(private val plugin: JavaPlugin) {

    var gameConfig: JinroGameConfig = JinroGameConfig()
        private set

    init {
        plugin.saveDefaultConfig()
        load()
    }

    /**
     * config.yml から設定を読み込む
     */
    fun load() {
        plugin.reloadConfig()
        val config = plugin.config

        // --- 役職設定の読み込み処理 ---
        val rolesMap = mutableMapOf<Role, Int>()
        val rolesSection = config.getConfigurationSection("settings.registered-roles")

        if (rolesSection != null) {
            for (key in rolesSection.getKeys(false)) {
                val role = runCatching { Role.valueOf(key.uppercase()) }.getOrNull()
                if (role != null) {
                    val count = rolesSection.getInt(key, 0)
                    if (count > 0) {
                        rolesMap[role] = count
                    }
                }
            }
        }

        // --- ショップ価格設定の読み込み処理 ⚠️追加 ---
        // デフォルト値のベースを用意しておく
        val defaultPrices = JinroGameConfig().shopPrices
        val pricesMap = defaultPrices.toMutableMap()

        val shopSection = config.getConfigurationSection("settings.shop-prices")
        if (shopSection != null) {
            for (key in shopSection.getKeys(false)) {
                // Configに保存されている価格を上書き読み込み（無ければデフォルト値）
                pricesMap[key] = shopSection.getInt(key, defaultPrices[key] ?: 0)
            }
        }

        // データの構築
        gameConfig = JinroGameConfig(
            firstDayTime = config.getInt("settings.first-day-time", 60),
            dayTime = config.getInt("settings.day-time", 180),
            nightTime = config.getInt("settings.night-time", 60),
            fieldType = config.getString("settings.field-type", "default") ?: "default",
            fixedWerewolfCount = config.getInt("settings.fixed-werewolf-count", 1),
            registeredRoles = rolesMap,
            shopPrices = pricesMap // ⚠️ 読み込んだショップ価格をセット
        )
    }

    /**
     * 現在の状態を config.yml に書き込んで保存する
     */
    fun save() {
        val config = plugin.config

        config.set("settings.first-day-time", gameConfig.firstDayTime)
        config.set("settings.day-time", gameConfig.dayTime)
        config.set("settings.night-time", gameConfig.nightTime)
        config.set("settings.field-type", gameConfig.fieldType)
        config.set("settings.fixed-werewolf-count", gameConfig.fixedWerewolfCount)

        // --- 役職設定の保存処理 ---
        config.set("settings.registered-roles", null)
        gameConfig.registeredRoles.forEach { (role, count) ->
            if (count > 0) {
                config.set("settings.registered-roles.${role.name}", count)
            }
        }

        // --- ショップ価格設定の保存処理 ⚠️追加 ---
        config.set("settings.shop-prices", null) // 一度クリア
        gameConfig.shopPrices.forEach { (itemKey, price) ->
            config.set("settings.shop-prices.$itemKey", price)
        }

        plugin.saveConfig()
    }
}