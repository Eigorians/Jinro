package com.eastcompany.eastsub.jinro.item.shop

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class CustomItemListener : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        val item = event.item ?: return
        val meta = item.itemMeta ?: return

        // 💡 PDCからアイテムIDを取得して判定する（より確実な方法）
        val itemId = meta.persistentDataContainer.get(CustomItem.ITEM_ID_KEY, PersistentDataType.STRING) ?: return
        val customItem = ItemRegistry.ITEMS[itemId] ?: return

        // 右クリック処理を実行
        val processed = customItem.handleRightClick(event.player, item)
        if (processed) {
            event.isCancelled = true
        }
    }
}