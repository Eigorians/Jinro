package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.util.TargetFinder
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Shinigami_Item : SpecialItem() {
    override val tag = "shinigami_0"
    override val lore = listOf("対象を\uE501で殺害し役職を奪う")
    override val material = Material.STICK

    override val role = Role.GRIM_REAPER // 💡 必要に応じてプロジェクトのEnum名に合わせてください
    override val displayName = role.roleName + "スキル 魂の強奪"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        // 冒頭：すでに使用不可フラグ（Byteの1）がPDCにあれば処理をキャンセルして終了
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0

            if (isCooltime == 1.toByte()) {
                return
            }
        }

        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()

        if (target !is ClickTarget.GamePlayerTarget) return

        val targetGamePlayer = target.gamePlayer

        val targetPlayer = targetGamePlayer.player ?: return

        val location = targetPlayer.location.add(0.0, 1.0, 0.0)

        // 演出（ターゲット側）
        targetPlayer.world.spawnParticle(Particle.SWEEP_ATTACK, location, 15, 0.2, 0.2, 0.2, 0.1)
        targetPlayer.world.playSound(location, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f)

        // 提示いただいた順序を完全に維持：共通化したメソッドでクールダウンアイテムに入れ替える
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        // 演出（使用者側）
        val particleLoc = player.location.add(0.0, 0.5, 0.0)
        player.world.spawnParticle(Particle.END_ROD, particleLoc, 8, 0.0, 0.0, 0.0, 0.2)

        player.world.playSound(player.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f)

        // 💡 ポンコツチェック（アサシンの役職一致チェックの順序を模倣）
        if (gamePlayer.role != this.role) return

        // === 死神の固有処理（役職の強奪・貫通ダメージ処理） ===

        // 相手の現在の役職を保持
        val stolenRole = targetGamePlayer.role

        // 自分の役職を相手の役職に書き換える（強奪）
        gamePlayer.role = stolenRole
        if(gamePlayer.camp != Camp.LOVERS && gamePlayer.camp != Camp.FOX) {
            gamePlayer.camp = role.camp
        }
        targetGamePlayer.role = Role.MURABITO
        targetGamePlayer.camp = Camp.VILLAGER

        player.sendMessage("あなたは ${targetPlayer.name} の命を奪い、[${stolenRole.roleName}] になりました。")

        JinroInventory().createRoleBook(gamePlayer, player, gamePlayer.role)

        targetPlayer.damage(40.0, player)
    }
}