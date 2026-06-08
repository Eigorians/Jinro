package com.eastcompany.eastsub.jinro.item.job

import com.eastcompany.eastsub.jinro.game.JinroPhase
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.Shufffler_Avility_Item
import com.eastcompany.eastsub.jinro.item.job.list.Roar_Item
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class SpecialItemManager(private val plugin: JavaPlugin) : Listener { // 💡 pluginをメンバ変数として保持するよう修正

    // 💡 アイテム特定用のPDCキー。CustomItem.create()内部で作られるキーと同じ名称・仕様に統一
    private val itemKey = NamespacedKey(plugin, "jinro_custom_item_tag")
    private val registry = HashMap<String, SpecialItem>()

    init {
        for (role in Role.entries) {
            val specialItem = role.specialItemFactory()
            register(specialItem)
        }
        register(Roar_Item())
        register(Shufffler_Avility_Item())
    }

    private fun register(item: SpecialItem) {
        registry[item.tag] = item
    }

    /**
     * ItemStackから対応するCustomItemオブジェクトを特定する
     */
    private fun getCustomItem(item: ItemStack?): SpecialItem? {
        if (item == null || !item.hasItemMeta()) return null
        val tag = item.itemMeta.persistentDataContainer.get(itemKey, PersistentDataType.STRING) ?: return null
        return registry[tag]
    }

    // --- メインの右クリックイベントのみで一括処理 ---
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!JinroGameManager.isGameRunning) return
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return // オフハンド重複防止
        val player = event.player
        val item = event.item ?: return
        val customItem = getCustomItem(item) ?: return
        if(JinroGameManager.currentPhase == JinroPhase.COURT){
            player.sendMessage(Component.text("裁判中には使用できません" , NamedTextColor.RED))
            return
        }
        val gamePlayer = JinroGameManager.gamePlayers[player.uniqueId] ?: return

        val currentEvent = event // ラムダ内で使うための参照

        Bukkit.getScheduler().runTask(plugin, Runnable {
            // プレイヤーがまだオンラインか念のためチェック
            if (!player.isOnline) return@Runnable

            // 💡 実際の処理（この中でアイテムをクールダウン用に書き換えても2回発火しなくなります）
            customItem.onRightClick(player, gamePlayer, currentEvent)
        })
    }

    /**
     * 💡 ターゲットプレイヤーのインベントリ内にある、すべての特別アイテムのクールダウンを解除する
     */
    fun resetAllCooldowns(player: Player) {
        val inventory = player.inventory

        // インベントリの全スロットをループ
        for (slot in 0 until inventory.size) {
            val item = inventory.getItem(slot) ?: continue

            // 手に持っているアイテムクラス（SpecialItem）を特定
            val specialItem = getCustomItem(item) ?: continue
            val container = item.itemMeta.persistentDataContainer

            // アイテム個別の cooldown_model_key からPDC用のキーを生成
            val cooltimeKey = NamespacedKey(plugin, specialItem.cooldown_model_key)

            // クールダウンフラグ（Byteの1）が埋め込まれているか確認
            val isCooltime = container.get(cooltimeKey, PersistentDataType.BYTE) ?: 0
            if (isCooltime == 1.toByte()) {
                // 通常の初期状態アイテム（使用可能状態）を新しく生成し、スロットを完全に上書き
                inventory.setItem(slot, specialItem.create())
            }
        }
    }

}