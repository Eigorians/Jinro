package com.eastcompany.eastsub.jinro.game

enum class Role(val roleName: String, val englishName: String, val camp: Camp) {
    // === 人狼陣営 ===
    WEREWOLF("人狼", "Werewolf", Camp.WEREWOLF),
    ASSASSIN("アサシン", "Assassin", Camp.WEREWOLF),
    SHUFFLER("シャッフラー", "Shuffler", Camp.WEREWOLF),

    // === 狂人陣営 === ⚠️ 陣営の変更
    MADMAN("狂人", "Madman", Camp.MADMAN),
    FANATIC("狂信者", "Fanatic", Camp.MADMAN),
    TRAPPER("罠師", "Trapper", Camp.MADMAN),

    // === 村人陣営 ===
    VILLAGER("村人", "Villager", Camp.VILLAGER),
    SEER("占い師", "Seer", Camp.VILLAGER),
    MEDIUM("霊能者", "Medium", Camp.VILLAGER),
    SHAMAN("シャーマン", "Shaman", Camp.VILLAGER),
    CLEANER("掃除屋", "Cleaner", Camp.VILLAGER),
    JUDGE("裁判官", "Judge", Camp.VILLAGER),
    KNIGHT("騎士", "Knight", Camp.VILLAGER),
    MASON("共有者", "Mason", Camp.VILLAGER),
    PONKOTSU("ポンコツ", "Ponkotsu", Camp.VILLAGER),
    BAKER("パン屋", "Baker", Camp.VILLAGER),
    OJOSAMA("お嬢様", "Ojosama", Camp.VILLAGER),
    THIEF("怪盗", "Thief", Camp.VILLAGER),
    MAGICAL_GIRL("魔法少女", "Magical Girl", Camp.VILLAGER),

    // === 狐陣営 ===
    FOX("妖狐", "Fox", Camp.FOX),
    BACKSLIDER("背徳者", "Backslider", Camp.FOX),

    // === てるてる陣営 ===
    FOOL("てるてる", "Fool", Camp.TERUTERU),

    // === 恋人陣営 ===
    CUPID("キューピット", "Cupid", Camp.LOVERS),

    // === 死神陣営 ===
    GRIM_REAPER("死神", "Grim Reaper", Camp.GRIM_REAPER)
}