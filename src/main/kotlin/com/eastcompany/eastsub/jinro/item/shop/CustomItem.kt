package com.eastcompany.eastsub.jinro.game.item

import com.eastcompany.eastsub.jinro.Jinro
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class CustomItem {
    abstract val id: String         // 💡 追加: 内部識別用のID ("bread_5", "iron_sword" など)
    abstract val material: Material
    abstract val displayName: String
    abstract val description: List<String>
    abstract val itemModel: String? // 空（null）なら非適用
    abstract val hasRightClickEffect: Boolean
    abstract val amount: Int

    companion object {
        // 💡 プラグイン全体で共有する識別用キー
        val ITEM_ID_KEY = NamespacedKey(Jinro.instance, "custom_item_id")
    }

    /**
     * 指定された個数でItemStackを生成するメソッド
     * 💡 引数を `amount: Int = this.amount` にすることで、未指定時はデフォルト個数になります
     */
    open fun create(amount: Int = this.amount): ItemStack {
        val item = ItemStack(material, amount)
        val meta = item.itemMeta ?: return item

        // デフォルトの名前、Lore、不壊設定、PDC登録
        meta.displayName(Component.text(displayName, NamedTextColor.GOLD))
        meta.lore(description.map { Component.text(it, NamedTextColor.WHITE) })
        if (!itemModel.isNullOrEmpty()) {
            val modelkey = "minecraft:item/" + itemModel
            NamespacedKey.fromString(modelkey)?.let { key -> meta.itemModel = key }
        }
        meta.isUnbreakable = true
        meta.persistentDataContainer.set(ITEM_ID_KEY, PersistentDataType.STRING, id)

        item.itemMeta = meta
        return item
    }

    /**
     * 右クリックしたときの効果を処理するメソッド
     * @return 効果が正常に発動した場合は true、不発（クールダウン中など）なら false
     */
    fun handleRightClick(player: Player, itemStack: ItemStack): Boolean {
        if (!hasRightClickEffect) return false

        // 各アイテム固有の効果を実行
        val success = onRightClick(player, itemStack)

        // 効果が発動したら持ち数を1減らす
        if (success) {
            itemStack.amount = itemStack.amount - 1
        }
        return success
    }

    /**
     * 個々のアイテムでオーバーライドして固有の効果を書くメソッド
     */
    protected open fun onRightClick(player: Player, itemStack: ItemStack): Boolean {
        return false
    }
}