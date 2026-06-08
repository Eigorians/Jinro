package com.eastcompany.eastsub.jinro.constant

import com.eastcompany.eastsub.jinro.Jinro
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

object JinroKeys {
    private val plugin: Jinro get() = Jinro.instance

    // --- アイテム関連のキー ---
    /** 役職専用アイテムを識別するためのPDCキー */
    val ITEM_TAG: NamespacedKey by lazy { NamespacedKey(plugin, "jinro_custom_item_tag") }


    // --- お墓関連のキー (werewolf:grave などを統一) ---
    /** 墓全体を識別するためのPDCキー */
    val GRAVE: NamespacedKey by lazy { NamespacedKey(plugin, "grave") }

    /** 墓の所有者(死者)のUUIDを識別するためのPDCキー */
    val GRAVE_OWNER: NamespacedKey by lazy { NamespacedKey(plugin, "grave_owner") }
}