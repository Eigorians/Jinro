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
    var lobby: String? = null,                   // ロビー（1箇所）
    var spawns: List<String> = emptyList(),      // スポーン位置（複数）
    var shops: List<String> = emptyList(),       // ショップ位置（複数）
    var court: String? = null,                   // 裁判所（1箇所）
    var undergroundY: Int? = null,               // 地下範囲のY座標
    var resourceLocations: List<String> = emptyList() // 復活資源ブロック（複数）
)