package com.eastcompany.eastsub.jinro.item.other

import com.eastcompany.eastsub.jinro.constant.JinroKeys
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class DummyItem {

    companion object {
        /**
         * ダミーアイテム（バリアブロックベース）を生成する
         */
        fun create(): ItemStack {
            // ベースはバリアブロック
            val item = ItemStack(Material.BARRIER)
            val meta = item.itemMeta ?: return item

            // 1. itemModel の設定 (minecraft:dummy)
            NamespacedKey.fromString("minecraft:other/dummy")?.let { modelKey ->
                meta.itemModel = modelKey
            }

            meta.displayName(Component.text("操作不能"))

            // 2. PDC (PersistentDataContainer) の設定
            meta.persistentDataContainer.set(JinroKeys.ITEM_TAG, PersistentDataType.STRING, "DUMMY")

            // メタをアイテムに適用して返す
            item.itemMeta = meta
            return item
        }

        /**
         * 💡 利便性のための判定メソッド：渡されたアイテムがこのダミーアイテムかどうかを調べる
         */
    }
}