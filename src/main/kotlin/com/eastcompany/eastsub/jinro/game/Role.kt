package com.eastcompany.eastsub.jinro.game

enum class Role(
    val roleName: String,
    val englishName: String,
    val camp: Camp,
    val character: String,     // サイドバー/チャット用アイコンフォント
    val role_view: String,     // ゲーム開始時に全画面表示する用のデカ文字フォント
    val role_book_1: String,   // 📖 役職図鑑・左ページ用画像 (\uE0xx / \uE1xx / \uE2xx)
    val role_book_r: String    // 📖 役職図鑑・右ページ用画像 (\uE0xx / \uE1xx / \uE2xx)
) {
    // ==========================================
    // 🔺 人狼陣営 (WEREWOLF)
    // ==========================================
    WEREWOLF("人狼", "Jinrou", Camp.WEREWOLF, "\uE000", "\uE002", "\uE001", "\uE002"),
    ASSASSIN("アサシン", "Assassin", Camp.WEREWOLF, "\uE004", "\uE006", "\uE009", "\uE00A"),
    SHUFFLER("シャッフラー", "Shuffler", Camp.WEREWOLF, "\uE005", "\uE007", "\uE00B", "\uE00C"),

    // ==========================================
    // 💀 狂人陣営 (MADMAN)
    // ==========================================
    MADMAN("狂人", "Kyoujin", Camp.MADMAN, "\uE001", "\uE003", "\uE003", "\uE004"),
    FANATIC("狂信者", "Kyoushin", Camp.MADMAN, "\uE002", "\uE004", "\uE005", "\uE006"),
    TRAPPER("罠師", "Wanashi", Camp.MADMAN, "\uE003", "\uE005", "\uE007", "\uE008"),

    // ==========================================
    // 🟢 村人陣営 (VILLAGER)
    // ==========================================
    VILLAGER("村人", "Mura", Camp.VILLAGER, "\uE100", "\uE100", "\uE100", "\uE101"),
    SEER("占い師", "Uranai", Camp.VILLAGER, "\uE101", "\uE101", "\uE102", "\uE103"),
    MEDIUM("霊能者", "Reinou", Camp.VILLAGER, "\uE102", "\uE102", "\uE104", "\uE105"),
    SHAMAN("シャーマン", "Shaman", Camp.VILLAGER, "\uE10E", "\uE10E", "\uE11C", "\uE11D"),
    CLEANER("掃除屋", "Soujiya", Camp.VILLAGER, "\uE104", "\uE104", "\uE108", "\uE109"),
    JUDGE("裁判官", "Saibankan", Camp.VILLAGER, "\uE109", "\uE109", "\uE112", "\uE113"),
    KNIGHT("騎士", "Kishi", Camp.VILLAGER, "\uE103", "\uE103", "\uE106", "\uE107"),
    MASON("共有者", "Kyouyuu", Camp.VILLAGER, "\uE105", "\uE105", "\uE10A", "\uE10B"),
    PONKOTSU("ポンコツ", "Ponkotsu", Camp.VILLAGER, "\uE107", "\uE107", "\uE110", "\uE111"),
    BAKER("パン屋", "Panya", Camp.VILLAGER, "\uE106", "\uE106", "\uE10C", "\uE10D"),
    OJOSAMA("お嬢様", "Ojousama", Camp.VILLAGER, "\uE108", "\uE108", "\uE10E", "\uE10F"),
    THIEF("怪盗", "Kaitou", Camp.VILLAGER, "\uE10B", "\uE10B", "\uE116", "\uE117"),
    MAGICAL_GIRL("魔法少女", "Mahou Shoujo", Camp.VILLAGER, "\uE110", "\uE110", "\uE120", "\uE121"),

    // ==========================================
    // 🦊 妖狐陣営 (FOX)
    // ==========================================
    FOX("妖狐", "Youko", Camp.FOX, "\uE200", "\uE200", "\uE200", "\uE201"),
    BACKSLIDER("背徳者", "Harikiri", Camp.FOX, "\uE10C", "\uE001", "\uE206", "\uE207"),

    // ==========================================
    // 🎈 てるてる陣営 (TERUTERU)
    // ==========================================
    FOOL("てるてる", "Teruteru", Camp.TERUTERU, "\uE10A", "\uE10A", "\uE114", "\uE115"),

    // ==========================================
    // 💘 恋人陣営 (LOVERS)
    // ==========================================
    CUPID("キューピット", "Cupid", Camp.LOVERS, "\uE202", "\uE202", "\uE204", "\uE205"),

    // ==========================================
    // 🔮 第三陣営・死神 (GRIM_REAPER)
    // ==========================================
    GRIM_REAPER("死神", "Shinigami", Camp.GRIM_REAPER, "\uE201", "\uE201", "\uE202", "\uE203");
}