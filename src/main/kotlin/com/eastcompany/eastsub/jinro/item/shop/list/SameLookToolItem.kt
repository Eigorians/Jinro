package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.constant.JinroKeys
import com.eastcompany.eastsub.jinro.game.item.CustomItem
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.time.Duration

object SameLookToolItem : CustomItem() {
    override val id = "disguise"
    override val material = Material.CARROT_ON_A_STICK

    override val displayName = "容姿統一ツール(右クリックで使用)"

    override val description = listOf(
        "[アイテム説明]",
        "全員の見た目が30秒間同一となる",
        "一度使うと消滅する"
    )

    override val itemModel = "same_look_tool"
    override val hasRightClickEffect = true
    override val amount = 1

    // データパック共通の指定フォント "announce"
    private val announceFont = Key.key("minecraft:announce")

    // 識別用のPDCキー（終了時に容姿統一の装備だけを狙って消去するため）
    private val armorTagKey = NamespacedKey(Jinro.instance, "same_look_armor")

    // 💡 現在実行中の終了タイマータスクを保持する変数
    private var currentEndTask: BukkitTask? = null

    override fun create(amount: Int): ItemStack {
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        meta.displayName(
            Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
        )

        val cleanedLore = listOf(
            Component.text("[アイテム説明]", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("全員の見た目が30秒間同一となる", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
            Component.text("一度使うと消滅する", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        )
        meta.lore(cleanedLore)

        meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
        meta.setEnchantmentGlintOverride(false)

        meta.addItemFlags(
            org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS,
            org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE
        )

        item.itemMeta = meta
        return item
    }

    override fun onRightClick(player: Player, itemStack: ItemStack): Boolean {
        val durationTicks = 600L // 30秒間 (30s = 600ticks)

        // タイトルの表示時間設定（フェードイン0秒 / 保持3秒 / フェードアウト0.5秒）
        val titleTimes = Title.Times.times(
            Duration.ofMillis(0),
            Duration.ofMillis(3000),
            Duration.ofMillis(500)
        )

        // 💡 既存のタイマータスクが動いている場合はキャンセルして延長する
        if (currentEndTask != null) {
            currentEndTask?.cancel()
            currentEndTask = null
            player.sendMessage(Component.text("容姿統一の効果時間が延長されました！", NamedTextColor.GOLD))
        } else {
            player.sendMessage(Component.text("容姿統一ツールを使用しました。", NamedTextColor.GREEN))
        }

        // 💡 容姿を統一するための専用防具の生成
        val helmet = createDisguiseArmor(Material.CHAINMAIL_HELMET, EquipmentSlot.HEAD)
        val chestplate = createDisguiseArmor(Material.CHAINMAIL_CHESTPLATE, EquipmentSlot.CHEST)
        val leggings = createDisguiseArmor(Material.CHAINMAIL_LEGGINGS, EquipmentSlot.LEGS)
        val boots = createDisguiseArmor(Material.CHAINMAIL_BOOTS, EquipmentSlot.FEET)

        val targetPlayers = player.world.players

        for (target in targetPlayers) {
            val gPlayer = JinroGameManager.gamePlayers[target.uniqueId] ?: continue
            if (!gPlayer.isAlive) continue

            // 💡 1. 防御力を皆無にする (0.0)
            target.getAttribute(Attribute.ARMOR)?.baseValue = 0.0

            // 💡 2. 透け防止の透明化 (30秒)
            // 延長時は効果時間を上書き（既存のエフェクトを一度消すか、durationを増やす。今回は一律30秒で再付与）
            target.removePotionEffect(PotionEffectType.INVISIBILITY)
            target.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, durationTicks.toInt(), 0, true, false))

            // 💡 3. 専用の統一防具を一斉装着 (すでに着ている場合も上書き)
            target.inventory.helmet = helmet
            target.inventory.chestplate = chestplate
            target.inventory.leggings = leggings
            target.inventory.boots = boots

            // 💡 4. アナウンスタイトル (\uE001) の表示
            val startTitle = Title.title(
                Component.text("\uE001").font(announceFont),
                Component.empty(),
                titleTimes
            )
            target.showTitle(startTitle)
        }

        // 💡 5. 30秒後に防具を外してステータスを戻すタイマー
        val task = object : BukkitRunnable() {
            override fun run() {
                // タスクが正常に実行されたら参照をクリア
                currentEndTask = null

                if (!JinroGameManager.isGameRunning) return

                for (target in player.world.players) {
                    val gPlayer = JinroGameManager.gamePlayers[target.uniqueId] ?: continue
                    if (!gPlayer.isAlive) continue

                    // 防御力を初期値に戻す
                    target.getAttribute(Attribute.ARMOR)?.baseValue = 0.0

                    // 容姿統一ツールで配られた防具だけを狙って安全に消去
                    if (isDisguiseArmor(target.inventory.helmet)) target.inventory.helmet = null
                    if (isDisguiseArmor(target.inventory.chestplate)) target.inventory.chestplate = null
                    if (isDisguiseArmor(target.inventory.leggings)) target.inventory.leggings = null
                    if (isDisguiseArmor(target.inventory.boots)) target.inventory.boots = null

                    // 終了アナウンスタイトル (\uE002) の表示
                    val endTitle = Title.title(
                        Component.text("\uE002").font(announceFont),
                        Component.empty(),
                        titleTimes
                    )
                    target.showTitle(endTitle)
                }
            }
        }

        // タスクを実行し、変数に保存
        currentEndTask = task.runTaskLater(Jinro.instance, durationTicks)

        // 使用したアイテムを消費させる
        itemStack.amount = itemStack.amount - 1

        return true
    }

    /**
     * 容姿統一用の防具を生成（PDCタグの埋め込み ＋ Equippableモデルの設定）
     */
    private fun createDisguiseArmor(material: Material, equipmentSlot: EquipmentSlot): ItemStack {
        val item = ItemStack(material)
        val meta = item.itemMeta ?: return item

        meta.persistentDataContainer.set(armorTagKey, PersistentDataType.STRING, "true")
        meta.setUnbreakable(true)
        meta.displayName(Component.text("容姿統一用防具", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))

        val equippable = meta.getEquippable()
        equippable.setSlot(equipmentSlot)
        equippable.setModel(NamespacedKey("minecraft", "same_armor"))
        meta.setEquippable(equippable)
        meta.persistentDataContainer.set(JinroKeys.ITEM_TAG, PersistentDataType.STRING, "ARMOR")
        item.itemMeta = meta
        return item
    }

    /**
     * 対象のアイテムが容姿統一ツールによって配られた防具かどうかを判定
     */
    private fun isDisguiseArmor(item: ItemStack?): Boolean {
        if (item == null || item.type == Material.AIR) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(armorTagKey, PersistentDataType.STRING)
    }
}