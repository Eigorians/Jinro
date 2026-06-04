package com.eastcompany.eastsub.jinro.game

enum class Camp(
    val campName: String,
    val icon: String,
    val soundName: String // 💡 サウンドのキー名のみ保持
) {
    VILLAGER("村人陣営", "\uE509", "minecraft:game_start_villager"),
    WEREWOLF("人狼陣営", "\uE507", "minecraft:game_start_wolf"),
    MADMAN("狂人陣営", "\uE508", "minecraft:game_start_wolf"), // 狂人も人狼音に

    // そのほか陣営
    FOX("狐陣営", "\uE50A", "minecraft:game_start_third"),
    TERUTERU("てるてる陣営", "\uE50A", "minecraft:game_start_third"),
    LOVERS("恋人陣営", "\uE50A", "minecraft:game_start_third"),
    GRIM_REAPER("死神陣営", "\uE50A", "minecraft:game_start_third")
}