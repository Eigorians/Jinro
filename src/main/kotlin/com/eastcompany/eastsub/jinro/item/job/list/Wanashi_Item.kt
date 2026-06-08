package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Wanashi_Item : SpecialItem() {
    override val tag = "wanashi_2"
    override val lore = listOf("罠を設置する")
    override val material = Material.BLAZE_ROD

    override val role = Role.TRAPPER
    override val displayName = role.roleName + "スキル　地面を\uE501で罠設置"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        if (player.hasCooldown(material)) return

        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0

            if (isCooltime == 1.toByte()) {
                return
            }
        }

        if (event.action != Action.RIGHT_CLICK_BLOCK) return
        val clickedBlock = event.clickedBlock ?: return

        // クリックされたブロックの1つ上のブロックを取得
        val blockAbove = clickedBlock.getRelative(BlockFace.UP)

        // 条件チェック: 1つ上のブロック（罠を置く予定の場所）に何もない（空気である）か
        if (!blockAbove.type.isAir) {
            player.sendMessage("§cその場所には罠を設置できません（上が塞がっています）。")
            return
        }

        // 💡 変更: 罠師（TRAPPER）でなければダミー罠（isDummyTrap = true）にする
        val isDummyTrap = gamePlayer.role != Role.TRAPPER
        val manager = JinroGameManager.trapManager ?: return

        // 引数を `blockAbove`（1つ上のブロック）にして渡す
        val success = manager.deployTrap(player, blockAbove, isDummyTrap)
        if (!success) return

        player.setCooldown(material, 600)
    }
}