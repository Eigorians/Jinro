package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object InvisibilityPotionItem : CustomItem() {
    override val id = "invisibility"
    // JSON: "name": "minecraft:potion"
    override val material = Material.POTION

    // JSON: "text": "透明化のポーション", "color": "white", "italic": false
    override val displayName = "透明化のポーション"

    // JSON の lore 配列を完全再現
    override val description = listOf(
        "[アイテム説明]",
        "飲むと10秒間透明になる"
    )

    override val itemModel = "invisibility_potion"
    override val hasRightClickEffect = false // 💡 バニラのポーションとしてそのまま飲めるためfalseでOK
    override val amount = 1

    /**
     * 親クラスの create メソッドをオーバーライドし、JSON内の高度なポーションコンポーネント設定を再現します。
     */
    override fun create(amount: Int): ItemStack {
        val item = super.create(amount) // 親クラスの基本生成（不壊・PDC登録）

        // Potion用の特別なメタデータ(PotionMeta)として取得・キャストする
        val meta = item.itemMeta as? PotionMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 (JSON通りに斜体をオフにし、色は白・灰に) ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("飲むと10秒間透明になる", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        )
        meta.lore(cleanedLore)

        // --- 3. ポーションの中身とエフェクトの設定 (JSON: potion_contents 再現) ---
        // 💡 10進数カラー「15987699」を Bukkit の Color オブジェクトに変換して適用
        meta.color = Color.fromRGB(15987699)

        // 💡 10秒間（10秒 * 20ticks = 200）、アンプリファイア0（レベル1）の透明化を付与
        // パーティクルは出さない（show_particles: false）、アイコンは出す（show_icon: true）を完全再現
        val invisibilityEffect = PotionEffect(
            PotionEffectType.INVISIBILITY,
            200,
            0,
            false, // ambient
            false, // 💡 show_particles: false
            true   // 💡 show_icon: true
        )
        meta.addCustomEffect(invisibilityEffect, true)

        // --- 4. バニラ標準のポーション効果ツールチップ（"透明化 (0:10)" など）を非表示にする ---
        // JSON 内の "minecraft:hide_additional_tooltip": {} を完全に再現
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ADDITIONAL_TOOLTIP, // 💡 これによりバニラの余計な説明テキストが消えます
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }
}