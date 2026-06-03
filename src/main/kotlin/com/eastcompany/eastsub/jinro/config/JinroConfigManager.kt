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

    fun load() {
        plugin.reloadConfig()
        val config = plugin.config

        // --- 役職設定の読み込み ---
        val rolesMap = mutableMapOf<Role, Int>()
        val rolesSection = config.getConfigurationSection("settings.registered-roles")
        if (rolesSection != null) {
            for (key in rolesSection.getKeys(false)) {
                val role = runCatching { Role.valueOf(key.uppercase()) }.getOrNull()
                if (role != null) {
                    val count = rolesSection.getInt(key, 0)
                    if (count > 0) rolesMap[role] = count
                }
            }
        }

        // --- ショップ価格設定の読み込み ---
        val defaultPrices = JinroGameConfig().shopPrices
        val pricesMap = defaultPrices.toMutableMap()
        val shopSection = config.getConfigurationSection("settings.shop-prices")
        if (shopSection != null) {
            for (key in shopSection.getKeys(false)) {
                pricesMap[key] = shopSection.getInt(key, defaultPrices[key] ?: 0)
            }
        }

        // --- マップリスト設定の読み込み ---
        val loadedMapList = config.getStringList("settings.map-list").ifEmpty { listOf("ノーマル") }
        val loadedSelectedMap = config.getString("settings.selected-map", "ノーマル") ?: "ノーマル"

        // --- マップ詳細データ(座標リスト)の読み込み処理 ⚠️追加 ---
        val loadedMapData = mutableMapOf<String, MapData>()
        val mapsSection = config.getConfigurationSection("maps")

        // リストに登録されている全マップのデータを走査して組み立てる
        for (mapName in loadedMapList) {
            val section = mapsSection?.getConfigurationSection(mapName)
            val data = MapData(
                lobby = section?.getString("lobby"),
                spawns = section?.getStringList("spawns") ?: emptyList(),
                shops = section?.getStringList("shops") ?: emptyList(),
                court = section?.getString("court"),
                undergroundY = if (section?.contains("underground-y") == true) section.getInt("underground-y") else null,
                resourceLocations = section?.getStringList("resources") ?: emptyList()
            )
            loadedMapData[mapName] = data
        }

        gameConfig = JinroGameConfig(
            firstDayTime = config.getInt("settings.first-day-time", 60),
            dayTime = config.getInt("settings.day-time", 180),
            nightTime = config.getInt("settings.night-time", 60),
            selectedMap = loadedSelectedMap,
            mapList = loadedMapList,
            fixedWerewolfCount = config.getInt("settings.fixed-werewolf-count", 1),
            registeredRoles = rolesMap,
            shopPrices = pricesMap,
            mapData = loadedMapData // ⚠️ 反映
        )
    }

    fun save() {
        val config = plugin.config

        config.set("settings.first-day-time", gameConfig.firstDayTime)
        config.set("settings.day-time", gameConfig.dayTime)
        config.set("settings.night-time", gameConfig.nightTime)
        config.set("settings.fixed-werewolf-count", gameConfig.fixedWerewolfCount)
        config.set("settings.selected-map", gameConfig.selectedMap)
        config.set("settings.map-list", gameConfig.mapList)

        // --- 役職設定の保存 ---
        config.set("settings.registered-roles", null)
        gameConfig.registeredRoles.forEach { (role, count) ->
            if (count > 0) config.set("settings.registered-roles.${role.name}", count)
        }

        // --- ショップ価格の保存 ---
        config.set("settings.shop-prices", null)
        gameConfig.shopPrices.forEach { (itemKey, price) ->
            config.set("settings.shop-prices.$itemKey", price)
        }

        // --- マップ詳細データ(座標リスト)の保存処理 ⚠️追加 ---
        config.set("maps", null) // 古いデータを初期化
        gameConfig.mapData.forEach { (mapName, data) ->
            config.set("maps.$mapName.lobby", data.lobby)
            config.set("maps.$mapName.spawns", data.spawns.ifEmpty { null })
            config.set("maps.$mapName.shops", data.shops.ifEmpty { null })
            config.set("maps.$mapName.court", data.court)
            config.set("maps.$mapName.underground-y", data.undergroundY)
            config.set("maps.$mapName.resources", data.resourceLocations.ifEmpty { null })
        }

        plugin.saveConfig()
    }
}