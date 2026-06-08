package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.item.CustomItem
import io.papermc.paper.block.BlockPredicate
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockType
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

object PickaxeItem : CustomItem() {
    override val id = "pickaxe"
    override val material = Material.IRON_PICKAXE
    override val displayName = "頑丈なツルハシ"

    override val description = listOf(
        "[アイテム説明]",
        "いくら使っても壊れる気配のないツルハシ",
        "攻撃するためのものではない",
        "‣ 採集可能: "
    )

    override val itemModel = null
    override val hasRightClickEffect = false
    override val amount = 1

    override fun create(amount: Int): ItemStack {
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        // --- 1. 表示名とLoreの装飾調整 ---
        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("いくら使っても壊れる気配のないツルハシ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("攻撃するためのものではない", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("‣ 採集可能: ", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
                .append(Component.text("", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        )
        meta.lore(cleanedLore)

        // --- 2. 攻撃力を 0 にする ---
        val zeroDamageModifier = AttributeModifier(
            NamespacedKey(Jinro.instance, "zero_attack_damage_pickaxe"),
            0.0,
            AttributeModifier.Operation.MULTIPLY_SCALAR_1,
            EquipmentSlotGroup.MAINHAND
        )
        meta.removeAttributeModifier(Attribute.ATTACK_DAMAGE)
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, zeroDamageModifier)

        // --- 3. エンチャント・光沢の上書き ---
        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        // --- 4. ツールチップの非表示設定 ---
        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta

        // --- 5. 破壊可能ブロックの設定 (最新APIに完全準拠) ---

        // ① 許可するブロックオブジェクトのリストを作成
        // --- 5. 破壊可能ブロックの設定 ---
        val ironOres = listOf(BlockType.IRON_ORE, BlockType.DEEPSLATE_IRON_ORE)

        val ironOresSet = io.papermc.paper.registry.set.RegistrySet.keySetFromValues(
            io.papermc.paper.registry.RegistryKey.BLOCK,
            ironOres
        )

        val blockPredicate = BlockPredicate.predicate()
            .blocks(ironOresSet)
            .build()

        val adventurePredicate = ItemAdventurePredicate.itemAdventurePredicate(listOf(blockPredicate))
        item.setData(DataComponentTypes.CAN_BREAK, adventurePredicate)

        // ⑤ 最後に ItemStack に CAN_BREAK コンポーネントを適用
        item.setData(DataComponentTypes.CAN_BREAK, adventurePredicate)

        return item
    }
}