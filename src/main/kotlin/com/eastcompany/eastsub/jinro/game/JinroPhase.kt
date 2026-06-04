package com.eastcompany.eastsub.jinro.game

import org.bukkit.boss.BarColor

enum class JinroPhase(val phaseName: String, val barColor: BarColor) {
    FIRST_DAY("昼", BarColor.PINK),
    DAY("昼", BarColor.YELLOW),
    COURT("裁判", BarColor.RED),
    NIGHT("夜", BarColor.BLUE)
}