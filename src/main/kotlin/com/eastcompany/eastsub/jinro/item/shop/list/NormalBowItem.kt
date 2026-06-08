package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object NormalBowItem : CustomItem() {
    override val id = "bow" // 💡 JSONのタグ指定 "normal_bow" と一致させています
    // JSON: "name": "minecraft:bow"
    override val material = Material.BOW

    // JSON: "text": "頑丈な弓", "color": "white", "italic": false
    override val displayName = "頑丈な弓"

    // JSON の lore 配列を完全再現
    override val description = listOf(
        "[アイテム説明]",
        "いくら使っても壊れる気配のない弓"
    )

    override val itemModel = null
    override val hasRightClickEffect = false // バニラの弓としてそのまま射撃させるためfalse
    override val amount = 1

    /**
     * 親クラスの create メソッドをオーバーライドし、JSON内のエンチャント・隠し設定を再現します。
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
            Component.text("いくら使っても壊れる気配のない弓", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        )
        meta.lore(cleanedLore)

        // --- 2. エンチャント: 消失の呪い(レベル1)を付与 & 光沢を消す (JSON: components 再現) ---
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        // --- 3. ツールチップの非表示設定 (JSON: "show_in_tooltip": false 再現) ---
        // エンチャントの紫文字と、親クラス側で付与される不壊の文字を非表示にします。
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }
}