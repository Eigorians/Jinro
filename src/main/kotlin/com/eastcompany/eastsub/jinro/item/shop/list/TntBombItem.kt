package com.eastcompany.eastsub.jinro.item.shop.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.item.CustomItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

object TntBombItem : CustomItem() {
    override val id = "bomb"
    override val material = Material.SNOWBALL
    override val displayName = "手投げ爆弾(右クリックで使用)"

    // データパックの仕様に合わせたLore [cite: 6]
    override val description = listOf(
        "[アイテム説明]",
        "投げるとすぐに爆発する",
        "スニークしながら投擲で起爆までの時間が延びる"
    )

    // 💡 1. 移行せず、リソースパックの itemModel をそのまま使用
    override val itemModel = "tnt_bomb"
    override val hasRightClickEffect = true
    override val amount = 1

    override fun create(amount: Int): ItemStack {
        // 親クラス（CustomItem）のcreateを呼び出すことで、自動的に itemModel が適用されます
        val item = super.create(amount)
        val meta = item.itemMeta ?: return item

        // 表示名とLoreの装飾をデータパックのJSON基準（白・灰・斜体オフ）に調整 [cite: 6]
        meta.displayName(Component.text(displayName, NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
        meta.lore(description.map { Component.text(it, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false) })

        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE)

        item.itemMeta = meta
        return item
    }

    override fun onRightClick(player: Player, itemStack: ItemStack): Boolean {
        val world = player.world
        // プレイヤーの視線方向から少し前方の座標を計算
        val spawnLoc = player.eyeLocation.add(player.location.direction.multiply(0.5))

        // スニーク状態（データパックでの charged 判定相当）の検知 [cite: 4]
        val isSneaking = player.isSneaking

        // 💡 1. 飛ばすItemStackを生成（itemModelが適用された状態）
        val bombItemStack = this.create(1)

        // アイテムエンティティとしてワールドにスポーン [cite: 3]
        val droppedItem = world.dropItem(spawnLoc, bombItemStack)

        // 💡 2. 誰も拾えないように設定 (データパックの PickupDelay: 10000 に同期) [cite: 3]
        droppedItem.pickupDelay = 10000

        // アイテムのヘルス値を設定 (データパックの Health: 1s に同期) [cite: 3]
        droppedItem.health = 1

        // 💡 3. 飛ばす速度（初速度）を落とす
        // 通常の雪玉（1.5）よりも遅い速度（0.8倍など）にして前方へ射出
        val velocity = player.location.direction.multiply(0.8)
        droppedItem.velocity = velocity

        // メタデータを付与
        droppedItem.setMetadata("is_tnt_bomb", FixedMetadataValue(Jinro.instance, true))

        // 💡 4. 数秒後に爆発するタイマー (データパックの tick 数に完全同期)
        // 通常時は15 Ticks (0.75秒), チャージ時は45 Ticks (2.25秒) [cite: 3]
        val delayTicks = if (isSneaking) 45L else 15L

        // 点火音の再生（データパック再現） [cite: 1]
        player.playSound(player.location, "minecraft:entity.tnt.primed", 1.0f, 1.0f)

        // タスクスケジューラで時間差爆発
        object : BukkitRunnable() {
            override fun run() {
                if (droppedItem.isValid && !droppedItem.isDead) {
                    executeExplosion(droppedItem, player)
                }
            }
        }.runTaskLater(Jinro.instance, delayTicks) // 💡 4. 指定tick数後に実行 [cite: 3]

        return true
    }

    /**
     * データパックの explosion.mcfunction のダメージ・パーティクル設定を移植した爆発処理
     */
    private fun executeExplosion(itemEntity: Item, shooter: Player) {
        val loc = itemEntity.location
        val world = loc.world ?: return

        // パーティクル生成 (explosion.mcfunction の再現) [cite: 1]
        world.spawnParticle(org.bukkit.Particle.EXPLOSION, loc.add(0.5, 0.0, 0.5), 10, 0.5, 0.5, 0.5, 0.0)
        world.spawnParticle(org.bukkit.Particle.FLAME, loc, 10, 0.0, 0.0, 0.0, 0.2)
        world.spawnParticle(org.bukkit.Particle.SMOKE, loc, 20, 0.5, 0.5, 0.5, 0.0)

        // サウンド再生 [cite: 1]
        world.playSound(loc, org.bukkit.Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)

        // 距離に応じた正確なダメージ減衰処理 (explosion.mcfunction の距離判定を再現) [cite: 1, 2]
        val radius = 5.0
        val nearbyEntities = world.getNearbyEntities(loc, radius, radius, radius)

        for (entity in nearbyEntities) {
            if (entity is Player) {
                val distance = entity.location.distance(loc)
                val damageAmount = when {
                    distance <= 3.0 -> 7.0               // 3m以内: ダメージ 7 [cite: 1]
                    distance in 3.00001..4.0 -> 3.0       // 3m〜4m: ダメージ 3 [cite: 2]
                    distance in 4.00001..5.0 -> 2.0       // 4m〜5m: ダメージ 2 [cite: 2]
                    else -> 0.0
                }

                if (damageAmount > 0.0) {
                    // ダメージソースを「爆発」、原因を投擲者にしてダメージを与える [cite: 1, 2]
                    entity.damage(damageAmount, shooter)
                }
            }
        }

        // 飛ばしたItemStackエンティティを消去 [cite: 1]
        itemEntity.remove()
    }
}