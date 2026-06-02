package com.eastcompany.eastsub.jinro.config

import com.eastcompany.eastsub.jinro.game.Role // ⚠️ 正しい人狼のRole Enumをインポート

/**
 * 人狼ゲームの設定データを保持するデータクラス
 */
data class JinroGameConfig(
    var firstDayTime: Int = 60,           // 初日の昼 (秒)
    var dayTime: Int = 180,              // 昼 (秒)
    var nightTime: Int = 60,             // 夜 (秒)
    var fieldType: String = "default",   // フィールド
    var fixedWerewolfCount: Int = 1,     // 人狼の人数固定
    var registeredRoles: Map<Role, Int> = emptyMap(), // 正しいRole型

    var shopPrices: Map<String, Int> = mapOf(
        "bread_1" to 1, "bread_5" to 2, "wood_sword" to 5, "iron_sword" to 5,
        "bow" to 5, "arrow" to 1, "axe" to 1, "pickaxe" to 1, "smoke" to 3,
        "bomb" to 5, "blind" to 5, "disguise" to 5, "glowing" to 5, "invisibility" to 5
    )
)