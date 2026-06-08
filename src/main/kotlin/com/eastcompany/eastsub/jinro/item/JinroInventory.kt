package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.other.RoleBookManager
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class JinroInventory {

    fun createRoleBook(gamePlayer: GamePlayer, player: Player, role: Role) {
        val personalRoleBook = RoleBookManager.createRoleBook(gamePlayer.role)
        player.inventory.setItem(7, personalRoleBook)
        player.inventory.setItem(8, gamePlayer.role.specialItem.create())

    }
}