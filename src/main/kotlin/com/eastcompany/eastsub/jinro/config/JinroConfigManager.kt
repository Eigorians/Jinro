package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role
import org.bukkit.Bukkit
import org.bukkit.Location
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

            // 文字列からLocationへ変換
            val lobby = section?.getString("lobby")?.let { parseLocation(it) }
            val spawns = section?.getStringList("spawns")?.mapNotNull { parseLocation(it) } ?: emptyList()
            val shops = section?.getStringList("shops")?.mapNotNull { parseLocation(it) } ?: emptyList()
            val court = section?.getString("court")?.let { parseLocation(it) }
            val resourceLocations = section?.getStringList("resources")?.mapNotNull { parseLocation(it) } ?: emptyList()

            val flowers = section?.getStringList("resources.flowers")?.mapNotNull { parseLocation(it) } ?: emptyList()
            val chests = section?.getStringList("resources.chests")?.mapNotNull { parseLocation(it) } ?: emptyList()
            val irons = section?.getStringList("resources.irons")?.mapNotNull { parseLocation(it) } ?: emptyList()
            val woods = section?.getStringList("resources.woods")?.mapNotNull { parseLocation(it) } ?: emptyList()

            val data = MapData(
                lobby = lobby,
                spawns = spawns,
                shops = shops,
                court = court,
                undergroundY = if (section?.contains("underground-y") == true) section.getInt("underground-y") else null,
                resourceFlowers = flowers,
                resourceChests = chests,
                resourceIrons = irons,
                resourceWoods = woods
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

    fun parseLocation(locStr: String): Location? {
        if (locStr.isBlank()) return null
        return runCatching {
            val parts = locStr.split(",")
            val world = Bukkit.getWorld(parts[0]) ?: return null
            Location(
                world,
                parts[1].toDouble(),
                parts[2].toDouble(),
                parts[3].toDouble(),
                parts.getOrNull(4)?.toFloat() ?: 0f,
                parts.getOrNull(5)?.toFloat() ?: 0f
            )
        }.getOrNull()
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
        config.set("maps", null)
        gameConfig.mapData.forEach { (mapName, data) ->
            val path = "maps.$mapName"

            config.set("$path.lobby", serializeLocation(data.lobby))
            config.set("$path.spawns", data.spawns.mapNotNull { serializeLocation(it) }.ifEmpty { null })
            config.set("$path.shops", data.shops.mapNotNull { serializeLocation(it) }.ifEmpty { null })
            config.set("$path.court", serializeLocation(data.court))
            config.set("$path.underground-y", data.undergroundY)

            // 💡 種類ごとにコンフィグの階層を分けて保存
            config.set("$path.resources.flowers", data.resourceFlowers.mapNotNull { serializeLocation(it) }.ifEmpty { null })
            config.set("$path.resources.chests", data.resourceChests.mapNotNull { serializeLocation(it) }.ifEmpty { null })
            config.set("$path.resources.irons", data.resourceIrons.mapNotNull { serializeLocation(it) }.ifEmpty { null })
            config.set("$path.resources.woods", data.resourceWoods.mapNotNull { serializeLocation(it) }.ifEmpty { null })
        }

        plugin.saveConfig()
    }

    private fun serializeLocation(loc: Location?): String? {
        if (loc == null) return null
        // ワールド名,x,y,z,yaw,pitch の形式で保存
        return "${loc.world?.name},${loc.x},${loc.y},${loc.z},${loc.yaw},${loc.pitch}"
    }
}