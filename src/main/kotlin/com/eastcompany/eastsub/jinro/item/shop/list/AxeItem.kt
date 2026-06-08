package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.item.CustomItem
import io.papermc.paper.block.BlockPredicate
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.BlockType
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

object AxeItem : CustomItem() {
    override val id = "axe"
    override val material = Material.IRON_AXE
    override val displayName = "頑丈な斧"

    override val description = listOf(
        "[アイテム説明]",
        "いくら使っても壊れる気配のない斧",
        "攻撃するためのものではない",
        "‣ 採集可能: "
    )

    override val itemModel = null
    override val hasRightClickEffect = false
    override val amount = 1

    /**
     * 💡 修正: 親クラスの create メソッドを完全にオーバーライドします。
     * これにより、ItemRegistry や ShopManager が `create()` を呼んだときにこのロジックが走ります。
     */
    override fun create(amount: Int): ItemStack {
        // super.create(amount) を呼ぶことで、親クラスの共通処理（不壊設定、PDCのID埋め込みなど）を先に実行する
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 (JSON通りに斜体をオフにし、色は白・灰に) ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("いくら使っても壊れる気配のない斧", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("攻撃するためのものではない", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("‣ 採集可能: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        )
        meta.lore(cleanedLore)

        // --- 2. 属性変更: 攻撃力を 0 にする (先ほど提示されたAttributeModifierクラスのOperationに適合) ---
        val zeroDamageModifier = AttributeModifier(
            NamespacedKey(Jinro.instance, "zero_attack_damage"),
            0.0,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1, // 💡 先ほどの定義から、乗算で確実に0にするのが最も安全です
            EquipmentSlotGroup.MAINHAND
        )
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, zeroDamageModifier)

        // --- 3. ツールチップの非表示設定 ---
        meta.isHideTooltip = false

        // --- 4. エンチャント: 消失の呪い(レベル1)を付与 & 光沢を消す ---
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        // --- 5. 不壊設定・エンチャント・属性のツールチップを非表示にする ---
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta

        val oakLogTypes = listOf(BlockType.STRIPPED_OAK_WOOD)

        // 1.20.6+ の RegistryKeySet を取得（前の手順で判明した安全な実装）
        val oakSet = io.papermc.paper.registry.set.RegistrySet.keySetFromValues(
            RegistryKey.BLOCK,
            oakLogTypes
        )

        val blockPredicate = BlockPredicate.predicate()
            .blocks(oakSet)
            .build()

        val adventurePredicate = ItemAdventurePredicate.itemAdventurePredicate(listOf(blockPredicate))

        // ItemStackにコンポーネントを適用
        item.setData(DataComponentTypes.CAN_BREAK, adventurePredicate)

        return item
    }
}