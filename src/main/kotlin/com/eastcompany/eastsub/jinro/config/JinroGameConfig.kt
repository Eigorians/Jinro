package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role

/**
 * 人狼ゲームの設定データを保持するデータクラス
 */
data class JinroGameConfig(
    var firstDayTime: Int = 60,
    var dayTime: Int = 180,
    var nightTime: Int = 60,
    var selectedMap: String = "ノーマル",
    var mapList: List<String> = listOf("ノーマル"),
    var fixedWerewolfCount: Int = 1,
    var registeredRoles: Map<Role, Int> = emptyMap(),

    // 💡 ユーザーが自由に変更できる現在の価格
    var shopPrices: Map<String, Int> = mapOf(
        "bread_1" to 1, "bread_5" to 2, "wood_sword" to 5, "iron_sword" to 5,
        "bow" to 5, "arrow" to 1, "axe" to 1, "pickaxe" to 1, "smoke" to 3,
        "bomb" to 5, "blind" to 5, "disguise" to 5, "glowing" to 5, "invisibility" to 5
    ),

    // ✨ 追加: 価格の変更判定に使うための「デフォルトの価格（初期値）」
    // 基準値として比較するだけなので、変更不可の val で定義します
    val defaultShopPrices: Map<String, Int> = mapOf(
        "bread_1" to 1, "bread_5" to 2, "wood_sword" to 5, "iron_sword" to 5,
        "bow" to 5, "arrow" to 1, "axe" to 1, "pickaxe" to 1, "smoke" to 3,
        "bomb" to 5, "blind" to 5, "disguise" to 5, "glowing" to 5, "invisibility" to 5
    ),

    // ⚠️ 追加: マップ名ごとの各座標データを管理する構造
    var mapData: Map<String, MapData> = emptyMap()
)

/**
 * 1つのマップ内に紐づくツール座標のデータ構造 (String型で座標を保持)
 */
data class MapData(
    var lobby: org.bukkit.Location? = null,
    var spawns: List<org.bukkit.Location> = emptyList(),
    var shops: List<org.bukkit.Location> = emptyList(),
    var court: org.bukkit.Location? = null,
    var undergroundY: Int? = null,

    // 💡 4つの資源ごとにリストを分離
    var resourceFlowers: List<org.bukkit.Location> = emptyList(),
    var resourceChests: List<org.bukkit.Location> = emptyList(),
    var resourceIrons: List<org.bukkit.Location> = emptyList(),
    var resourceWoods: List<org.bukkit.Location> = emptyList()
)