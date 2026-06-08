package com.eastcompany.eastsub.jinro.item.job

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.constant.JinroKeys
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class SpecialItem {
    abstract val tag: String
    abstract val displayName: String
    abstract val lore: List<String>
    abstract val role: Role
    abstract val model_key: String
    // 💡 追加：クールダウン状態のモデルキー・PDC判定用キー
    abstract val cooldown_model_key: String

    open val material: Material = Material.STICK

    private val plugin: Jinro get() = Jinro.instance

    /**
     * 通常状態のItemStackを生成する
     */
    fun create(): ItemStack {

        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                this.displayName(Component.text(this@SpecialItem.displayName))
                this.lore(this@SpecialItem.lore.map { Component.text(it) })
                this.persistentDataContainer.set(JinroKeys.ITEM_TAG, PersistentDataType.STRING, this@SpecialItem.tag)

                val fullModelString = if (this@SpecialItem.model_key.contains(":")) {
                    this@SpecialItem.model_key
                } else {
                    "minecraft:skill/${this@SpecialItem.model_key}"
                }

                NamespacedKey.fromString(fullModelString)?.let { modelKey ->
                    this.itemModel = modelKey
                }
            }
        }
    }

    /**
     * クールダウン状態（使用不能）のItemStackを生成する
     */
    fun createCooldownItem(): ItemStack {
        // 💡 cooldown_model_key をそのままPDCキー名として使用
        val cooltimeKey = NamespacedKey(plugin, this.cooldown_model_key)

        return ItemStack(material).apply {
            itemMeta = itemMeta?.apply {
                this.displayName(Component.text(this@SpecialItem.displayName + "　-使用不能-"))
                this.lore(this@SpecialItem.lore.map { Component.text(it) })

                // 1. マネージャー認識用の元のタグを保持
                this.persistentDataContainer.set(JinroKeys.ITEM_TAG, PersistentDataType.STRING, this@SpecialItem.tag)

                // 2. 使用不可フラグ（Byteの1）をPDCに埋め込む
                this.persistentDataContainer.set(cooltimeKey, PersistentDataType.BYTE, 1.toByte())

                // 3. cooldown_model_key から item_model を設定
                val fullModelString = if (this@SpecialItem.cooldown_model_key.contains(":")) {
                    this@SpecialItem.cooldown_model_key
                } else {
                    "minecraft:skill/${this@SpecialItem.cooldown_model_key}"
                }

                NamespacedKey.fromString(fullModelString)?.let { modelKey ->
                    this.itemModel = modelKey
                }
            }
        }
    }

    // 💡 追加: 試合中に特定の役職アイテムを捨てるのをキャンセルする
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (!JinroGameManager.isGameRunning) return

        val itemStack = event.itemDrop.itemStack
        val meta = itemStack.itemMeta ?: return

        // 💡 1行でスッキリ判定できるようになります！
        if (meta.persistentDataContainer.has(JinroKeys.ITEM_TAG, PersistentDataType.STRING)) {
            event.isCancelled = true
            event.player.sendMessage(Component.text("⚠️ 試合中に役職専用アイテムを捨てることはできません。", NamedTextColor.RED))
        }
    }

    open fun onRightClick(player: Player, gamePlayer: GamePlayer, event: PlayerInteractEvent) {}
}