package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import org.bukkit.Material

object IronSwordItem : CustomItem() {
    override val id = "iron_sword" // 💡 PDC識別用の固有IDを追加する場合
    override val material = Material.IRON_SWORD
    override val displayName = "鉄の剣"
    override val description = listOf("鋭い鉄製の剣。", "人狼を倒すために必要。")
    override val itemModel = null
    override val hasRightClickEffect = false
    override val amount = 1
}