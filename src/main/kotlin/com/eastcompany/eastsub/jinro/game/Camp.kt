package com.eastcompany.eastsub.jinro.game

enum class Camp(val campName: String, val icon: String) {
    VILLAGER("村人陣営", "\uE509"),
    WEREWOLF("人狼陣営", "\uE507"),
    MADMAN("狂人陣営", "\uE508"),

    // そのほか陣営はすべて「\uE50A」に統一
    FOX("狐陣営", "\uE50A"),
    TERUTERU("てるてる陣営", "\uE50A"),
    LOVERS("恋人陣営", "\uE50A"),
    GRIM_REAPER("死神陣営", "\uE50A")
}