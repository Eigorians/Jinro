package com.eastcompany.eastsub.jinro.game

enum class Camp(
    val campName: String,
    val icon: String,
    val soundName: String, // サウンドのキー名
    val result: String     // result.jsonの「chars」に対応
) {
    // 0: victory_wolf.png
    JINRO("人狼陣営", "\uE507", "minecraft:game_start_wolf", "0"),

    // 1: victory_villager.png
    VILLAGER("村人陣営", "\uE509", "minecraft:game_start_villager", "1"),

    // 2: victory_maniac.png (狂人)
    KYOJIN("狂人陣営", "\uE508", "minecraft:game_start_wolf", "2"),

    // 3: victory_youko.png (狐)
    FOX("狐陣営", "\uE50A", "minecraft:game_start_third", "3"),

    // 4: victory_shinigami.png (死神)
    GRIM_REAPER("死神陣営", "\uE50A", "minecraft:game_start_third", "4"),

    // 5: victory_cupid.png (恋人 / キューピッド)
    LOVERS("恋人陣営", "\uE50A", "minecraft:game_start_third", "5"),

    // 6: victory_teruteru.png (てるてる)
    TERUTERU("てるてる陣営", "\uE50A", "minecraft:game_start_third", "6"),
}