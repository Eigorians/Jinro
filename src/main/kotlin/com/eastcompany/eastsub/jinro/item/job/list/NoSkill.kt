package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import org.bukkit.Material

class NoSkill : SpecialItem() {
    override val tag = "noskill"
    override val lore = listOf("スキルなし")
    override val material = Material.STICK

    override val role = Role.MURABITO
    override val displayName = role.roleName + "スキルなし"
    override val model_key = tag
    override val cooldown_model_key = model_key

}