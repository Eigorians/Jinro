import com.eastcompany.eastsub.jinro.game.Camp

enum class Role(val roleName: String, val camp: Camp) {
    // === 人狼陣営 ===
    WEREWOLF("人狼", Camp.WEREWOLF),
    ASSASSIN("アサシン", Camp.WEREWOLF),
    SHUFFLER("シャッフラー", Camp.WEREWOLF),
    MADMAN("狂人", Camp.WEREWOLF),
    FANATIC("狂信者", Camp.WEREWOLF),
    TRAPPER("罠師", Camp.WEREWOLF),

    // === 村人陣営 ===
    VILLAGER("村人", Camp.VILLAGER),
    SEER("占い師", Camp.VILLAGER),
    MEDIUM("霊能者", Camp.VILLAGER),
    SHAMAN("シャーマン", Camp.VILLAGER),
    CLEANER("掃除屋", Camp.VILLAGER),
    JUDGE("裁判官", Camp.VILLAGER),
    KNIGHT("騎士", Camp.VILLAGER),
    MASON("共有者", Camp.VILLAGER),
    PONKOTSU("ポンコツ", Camp.VILLAGER),
    BAKER("パン屋", Camp.VILLAGER),
    OJOSAMA("お嬢様", Camp.VILLAGER),
    THIEF("怪盗", Camp.VILLAGER),
    MAGICAL_GIRL("魔法少女", Camp.VILLAGER),

    // === 狐陣営 ===
    FOX("妖狐", Camp.FOX),
    BACKSLIDER("背徳者", Camp.FOX), // 👈 追加！ 陣営は狐陣営になります

    // === てるてる陣営 ===
    FOOL("てるてる", Camp.TERUTERU),

    // === 恋人陣営 ===
    CUPID("キューピット", Camp.LOVERS),

    // === 死神陣営 ===
    GRIM_REAPER("死神", Camp.GRIM_REAPER)
}