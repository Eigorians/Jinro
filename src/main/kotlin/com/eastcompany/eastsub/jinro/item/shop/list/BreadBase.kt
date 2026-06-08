package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * パンのベースとなる共通設定を保持するヘルパークラス
 */
private object BreadBase {
    val material = Material.BREAD
    val displayName = "焼きたてのパン"
    val description = listOf(
        "[アイテム説明]",
        "美味しそうなパン",
        "焼きたての良い香りがする",
        "‣ Food: "
    )

    /**
     * JSONの装飾や特殊フォントを再現してItemStackを生成する
     */
    fun buildItemStack(customItem: CustomItem, amount: Int): ItemStack {
        // 親クラス（CustomItem）の標準生成処理（不壊設定、PDCへのID埋め込みなど）を走らせる
        val item = ItemStack(customItem.material, amount)
        val meta = item.itemMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 (JSON通りに斜体をオフにし、色は白・灰に) ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("美味しそうなパン", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("焼きたての良い香りがする", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("‣ Food: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        )
        meta.lore(cleanedLore)

        // --- 2. 不壊設定のツールチップを非表示にする ---
        meta.isUnbreakable = true
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE)

        // --- 3. 識別用IDをPDCに埋め込む ---
        meta.persistentDataContainer.set(
            CustomItem.ITEM_ID_KEY,
            org.bukkit.persistence.PersistentDataType.STRING,
            customItem.id
        )

        item.itemMeta = meta
        return item
    }
}

/**
 * 💡 1個売りのパン (Configの "bread_1" に対応)
 */
object BreadOneItem : CustomItem() {
    override val id = "bread_1"
    override val material = BreadBase.material
    override val displayName = BreadBase.displayName
    override val description = BreadBase.description
    override val itemModel = null
    override val hasRightClickEffect = false // バニラでそのまま食べられるためfalseでOK
    override val amount = 1

    override fun create(amount: Int): ItemStack {
        return BreadBase.buildItemStack(this, amount)
    }
}

/**
 * 💡 5個パックのパン (Configの "bread_5" に対応)
 */
object BreadFiveItem : CustomItem() {
    override val id = "bread_5"
    override val material = BreadBase.material
    override val displayName = BreadBase.displayName // 💡 必要なら "焼きたてのパン (5個パック)" などに変えてもOKです
    override val description = BreadBase.description
    override val itemModel = null
    override val hasRightClickEffect = false
    override val amount = 5 // デフォルトで5個生成される

    override fun create(amount: Int): ItemStack {
        return BreadBase.buildItemStack(this, amount)
    }
}