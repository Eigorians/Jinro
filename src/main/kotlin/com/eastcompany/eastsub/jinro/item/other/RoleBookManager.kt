package com.eastcompany.eastsub.jinro.item.other

import com.eastcompany.eastsub.jinro.constant.JinroKeys
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.Role
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.persistence.PersistentDataType

object RoleBookManager {

    private val bookFont = Key.key("minecraft:role_book")

    /**
     * 📖 指定された役職（Role）専用の解説本を生成する
     */
    fun createRoleBook(role: Role): ItemStack {
        val book = ItemStack(Material.WRITTEN_BOOK)
        val meta = book.itemMeta as? BookMeta ?: return book

        // 本のタイトル・製作者を設定
        meta.title(Component.text("役職本"))
        meta.author(Component.text("east_company"))

        // ─── 🏷️ 参考コードに合わせた item_model の設定 ───
        val modelId = when (role.camp) {
            Camp.VILLAGER -> "camp_villager"
            Camp.JINRO, Camp.KYOJIN -> "camp_wolf"
            else -> "camp_third"
        }

        // NamespacedKey を生成して setItemModel に流し込む
        val modelKey = NamespacedKey.fromString("minecraft:book/$modelId")
        meta.setItemModel(modelKey)

        // プレイヤーの役職データのみを流し込んで1ページ作成
        val pageComponent = Component.text()
            .append(Component.text("\uF990\uE000\uF991"))
            .append(Component.text(role.role_book_1)) // 割り当てられた役職の左ページ
            .append(Component.text("\uF992"))
            .append(Component.text(role.role_book_r)) // 割り当てられた役職の右ページ
            .append(Component.text("\uF993"))
            .font(bookFont)
            .color(NamedTextColor.WHITE)
            .build()

        meta.persistentDataContainer.set(JinroKeys.ITEM_TAG, PersistentDataType.STRING, "Book")
        meta.addPages(pageComponent)
        book.itemMeta = meta
        return book
    }
}