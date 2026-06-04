package com.eastcompany.eastsub.jinro.util

import org.bukkit.Location
import kotlin.math.round

object LocationUtil {
    /**
     * 座標をブロックの中心に据え、視線を90度刻みにきれいに整形する
     */
    fun cleanLocation(loc: Location): Location {
        val cleaned = loc.clone()
        // ブロックの中心に補正
        cleaned.x = loc.blockX + 0.5
        cleaned.z = loc.blockZ + 0.5

        // Yは足元（整数）
        cleaned.y = loc.blockY.toDouble()

        // Yawを90度刻みに整形 (0, 90, 180, 270 ...)
        cleaned.yaw = (round(loc.yaw / 90.0) * 90.0).toFloat()
        // Pitchを90度刻みに整形 (0, 90, -90)
        cleaned.pitch = (round(loc.pitch / 90.0) * 90.0).toFloat()

        return cleaned
    }
}