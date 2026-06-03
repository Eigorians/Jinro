package com.eastcompany.eastsub.jinro.game

enum class Role(
    val roleName: String,
    val englishName: String,
    val camp: Camp,
    val character: String // ✨ 新しいrole_view.jsonのUnicodeに更新
) {
    // === 人狼陣営 ===
    WEREWOLF("人狼", "Werewolf", Camp.WEREWOLF, "\uE000"),
    ASSASSIN("アサシン", "Assassin", Camp.WEREWOLF, "\uE004"),
    SHUFFLER("シャッフラー", "Shuffler", Camp.WEREWOLF, "\uE005"),

    // === 狂人陣営 ===
    MADMAN("狂人", "Madman", Camp.MADMAN, "\uE001"),
    FANATIC("狂信者", "Fanatic", Camp.MADMAN, "\uE002"),
    TRAPPER("罠師", "Trapper", Camp.MADMAN, "\uE003"),

    // === 村人陣営 ===
    VILLAGER("村人", "Villager", Camp.VILLAGER, "\uE100"),
    SEER("占い師", "Seer", Camp.VILLAGER, "\uE101"),
    MEDIUM("霊能者", "Medium", Camp.VILLAGER, "\uE102"),
    SHAMAN("シャーマン", "Shaman", Camp.VILLAGER, "\uE10E"),
    CLEANER("掃除屋", "Cleaner", Camp.VILLAGER, "\uE104"),
    JUDGE("裁判官", "Judge", Camp.VILLAGER, "\uE109"),
    KNIGHT("騎士", "Knight", Camp.VILLAGER, "\uE103"),
    MASON("共有者", "Mason", Camp.VILLAGER, "\uE105"),
    PONKOTSU("ポンコツ", "Ponkotsu", Camp.VILLAGER, "\uE107"), // 📝 role_viewのponkotsuは\uE107
    BAKER("パン屋", "Baker", Camp.VILLAGER, "\uE106"),
    OJOSAMA("お嬢様", "Ojosama", Camp.VILLAGER, "\uE108"), // 📝 role_viewのojousamaは\uE108
    THIEF("怪盗", "Thief", Camp.VILLAGER, "\uE10B"),
    MAGICAL_GIRL("魔法少女", "Magical Girl", Camp.VILLAGER, "\uE110"),

    // === 狐陣営 ===
    FOX("妖狐", "Fox", Camp.FOX, "\uE200"),
    BACKSLIDER("背徳者", "Backslider", Camp.FOX, "\uE10C"), // harikiri.png

    // === てるてる陣営 ===
    FOOL("てるてる", "Fool", Camp.TERUTERU, "\uE10A"),

    // === 恋人陣営 ===
    CUPID("キューピット", "Cupid", Camp.LOVERS, "\uE202"),

    // === 死神陣営 ===
    GRIM_REAPER("死神", "Grim Reaper", Camp.GRIM_REAPER, "\uE201")
}