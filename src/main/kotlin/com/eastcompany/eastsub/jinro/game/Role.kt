package com.eastcompany.eastsub.jinro.game

import com.eastcompany.eastsub.jinro.item.*
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.item.job.list.Reinou_Item
import com.eastcompany.eastsub.jinro.item.job.list.Shaman_Item
import com.eastcompany.eastsub.jinro.item.job.list.Uranaishi_Item
import com.eastcompany.eastsub.jinro.item.job.list.Youko_Item

enum class Role(
    val roleName: String,
    val englishName: String,
    val camp: Camp,
    val character: String,
    val role_view: String,
    val role_book_1: String,
    val role_book_r: String,
    // 💡 変更: 直接インスタンスを持たず、生成するための関数（ラムダ）を受け取る
    val specialItemFactory: () -> SpecialItem
) {
    // ==========================================
    // 🔺 人狼陣営 (WEREWOLF)
    // ==========================================
    JINRO("人狼", "Jinrou", Camp.JINRO, "\uE000", "\uE002", "\uE001", "\uE002" , { Jinro_Item() }),
    ASSASSIN("アサシン", "Assassin", Camp.JINRO, "\uE004", "\uE006", "\uE009", "\uE00A", { Assasin_Item() }),
    SHUFFLER("シャッフラー", "Shuffler", Camp.JINRO, "\uE005", "\uE007", "\uE00B", "\uE00C", { Shufffler_Item() }),

    // ==========================================
    // 💀 狂人陣営 (MADMAN)
    // ==========================================
    KYOUJIN("狂人", "Kyoujin", Camp.KYOJIN, "\uE001", "\uE003", "\uE003", "\uE004" , { NoSkill() }),
    KYOUSHIN("狂信者", "Kyoushin", Camp.KYOJIN, "\uE002", "\uE004", "\uE005", "\uE006", { NoSkill() }),
    TRAPPER("罠師", "Wanashi", Camp.KYOJIN, "\uE003", "\uE005", "\uE007", "\uE008" , { Wanashi_Item() }),

    // ==========================================
    // 🟢 村人陣営 (VILLAGER)
    // ==========================================
    MURABITO("村人", "Mura", Camp.VILLAGER, "\uE100", "\uE100", "\uE100", "\uE101" , { NoSkill() }),
    URANAI("占い師", "Uranai", Camp.VILLAGER, "\uE101", "\uE101", "\uE102", "\uE103" , { Uranaishi_Item() }),
    REINOU("霊能者", "Reinou", Camp.VILLAGER, "\uE102", "\uE102", "\uE104", "\uE105" , { Reinou_Item() }),
    SHAMAN("シャーマン", "Shaman", Camp.VILLAGER, "\uE10E", "\uE10E", "\uE11C", "\uE11D", { Shaman_Item() }),
    SOUJIYA("掃除屋", "Soujiya", Camp.VILLAGER, "\uE104", "\uE104", "\uE108", "\uE109", { Soujiya_Item() }),
    SAIBAN("裁判官", "Saibankan", Camp.VILLAGER, "\uE109", "\uE109", "\uE112", "\uE113", { Saibankan_Item() }),
    KISHI("騎士", "Kishi", Camp.VILLAGER, "\uE103", "\uE103", "\uE106", "\uE107", { Kishi_Item() }),
    KYOUYUU("共有者", "Kyouyuu", Camp.VILLAGER, "\uE105", "\uE105", "\uE10A", "\uE10B", { NoSkill() }),
    PONKOTSU("ポンコツ", "Ponkotsu", Camp.VILLAGER, "\uE107", "\uE107", "\uE110", "\uE111", { NoSkill() }),
    PANYA("パン屋", "Panya", Camp.VILLAGER, "\uE106", "\uE106", "\uE10C", "\uE10D", { NoSkill() }),
    OJOSAMA("お嬢様", "Ojousama", Camp.VILLAGER, "\uE108", "\uE108", "\uE10E", "\uE10F", { NoSkill() }),
    KAITOU("怪盗", "Kaitou", Camp.VILLAGER, "\uE10B", "\uE10B", "\uE116", "\uE117", { Kaitou_Item() }),
    MAGICAL_GIRL("魔法少女", "Mahou Shoujo", Camp.VILLAGER, "\uE110", "\uE110", "\uE120", "\uE121", { NoSkill() }),

    // ==========================================
    // 🦊 妖狐陣営 (FOX)
    // ==========================================
    FOX("妖狐", "Youko", Camp.FOX, "\uE200", "\uE200", "\uE200", "\uE201", { Youko_Item() }),

    // ==========================================
    // 🎈 てるてる陣営 (TERUTERU)
    // ==========================================
    TERUTERU("てるてる", "Teruteru", Camp.TERUTERU, "\uE10A", "\uE10A", "\uE114", "\uE115", { NoSkill() }),

    // ==========================================
    // 💘 恋人陣営 (LOVERS)
    // ==========================================
    CUPID("キューピット", "Cupid", Camp.LOVERS, "\uE202", "\uE202", "\uE204", "\uE205", { NoSkill() }),

    // ==========================================
    // 🔮 第三陣営・死神 (GRIM_REAPER)
    // ==========================================
    GRIM_REAPER("死神", "Shinigami", Camp.GRIM_REAPER, "\uE201", "\uE201", "\uE202", "\uE203", { Shinigami_Item() });

    // 💡 外部から「role.specialItem」として取得できるようにプロパティを定義しておく
    val specialItem: SpecialItem
        get() = specialItemFactory()
}