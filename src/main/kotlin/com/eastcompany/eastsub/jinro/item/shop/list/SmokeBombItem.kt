package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object SmokeBombItem : CustomItem() {
    override val id = "smoke_bomb" // 💡 JSONのタグ指定 "smoke_bomb" と一致させています
    // JSON: "name": "minecraft:snowball"
    override val material = Material.SNOWBALL

    // JSON: "text": "煙玉(右クリックで使用)", "color": "white", "italic": false
    override val displayName = "煙玉(右クリックで使用)"

    // JSON の lore 配列を完全再現
    override val description = listOf(
        "[アイテム説明]",
        "投げるとすぐに煙が噴き出す",
        "スニークしながら投擲で起爆までの時間が延びる"
    )

    override val itemModel = "smoke_bomb"
    override val hasRightClickEffect = false // 💡 雪玉として投げるため、独自の右クリック処理はfalse
    override val amount = 1

    /**
     * 親クラスの create メソッドをオーバーライドし、JSON内のカスタムモデルデータや隠し設定を再現します。
     */
    override fun create(amount: Int): ItemStack {
        val item = super.create(amount) // 親クラスの基本生成（不壊・PDC登録）
        val meta = item.itemMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 (JSON通りに斜体をオフにし、色は白・灰に) ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("投げるとすぐに煙が噴き出す", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("スニークしながら投擲で起爆までの時間が延びる", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        )
        meta.lore(cleanedLore)

        // --- 3. ツールチップの非表示設定 ---
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }
}