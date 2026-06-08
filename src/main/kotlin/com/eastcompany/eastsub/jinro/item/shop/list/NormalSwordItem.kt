package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object NormalSwordItem : CustomItem() {
    override val id = "wood_sword" // 💡 JSONの代表タグ "normal_sword" と一致させています
    // JSON: "name": "minecraft:carrot_on_a_stick"
    override val material = Material.WOODEN_SWORD

    // JSON: "text": "ボロい剣", "color": "white", "italic": false
    override val displayName = "ボロい剣"

    // JSON の lore 配列を完全再現（※最後の行に特殊フォントアイコン「」を含める）
    override val description = listOf(
        "[アイテム説明]",
        "使い込まれたボロボロの剣",
        "壊れることはない",
        "‣ Attack: "
    )

    override val itemModel = null
    override val hasRightClickEffect = false // 右クリック時の独自効果はなし
    override val amount = 1

    /**
     * 親クラスの create メソッドをオーバーライドし、JSON内のエンチャント・カスタムモデル・隠し設定を再現します。
     */
    override fun create(amount: Int): ItemStack {
        val item = super.create(amount) // 親クラスの基本生成（不壊設定・PDCへのID埋め込みなど）
        val meta = item.itemMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 (JSON通りに斜体をオフにし、色は白・灰に) ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("使い込まれたボロボロの剣", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("壊れることはない", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("‣ Attack: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        )
        meta.lore(cleanedLore)

        // --- 3. エンチャント: 消失の呪い(レベル1)を付与 & 光沢を消す (JSON: components 再現) ---
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        // --- 4. ツールチップの非表示設定 (JSON: "show_in_tooltip": false 再現) ---
        // エンチャントの紫文字と、親クラス側で付与される不壊の文字を非表示にします。
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }
}