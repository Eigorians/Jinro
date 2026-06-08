package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.manager.JinroGameManager.gamePlayers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Shufffler_Avility_Item : SpecialItem() {
    override val tag = "shuffler_avility_2"
    override val lore = listOf("プレイヤーの位置を\uE501でランダムにシャッフル \uE500 + \uE501でスキル切り替え")
    override val material = Material.BLAZE_ROD

    override val role = Role.SHUFFLER
    override val displayName = role.roleName + "スキル \uE501で全員の位置を入れ替え \uE500 + \uE501でスキル切り替え"
    override val model_key = "shuffler_2"
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

                if(player.isSneaking) {
                    player.inventory.setItemInMainHand(Shufffler_Item().createCooldownItem())
                }
                return
            }
            if(player.isSneaking) {
                player.inventory.setItemInMainHand(Shufffler_Item().create())
                return
            }
        }

        if(player.hasCooldown(material))return

        player.setCooldown(material, 3600)

        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        if (gamePlayer.role != this.role) return
        shuffleAlivePlayersLocation()
    }
    fun shuffleAlivePlayersLocation() {


        // 1. 生存中のプレイヤー（オンライン）とその現在の座標をリスト化
        val alivePlayers = gamePlayers.values.filter { it.isAlive && it.player != null }
        if (alivePlayers.size <= 1) return // 1人以下なら入れ替える必要なし

        // プレイヤーの現在の位置（Location）をコピーしてリストにする
        val locations = alivePlayers.map { it.player!!.location }.toMutableList()

        // 2. 座標リストをランダムにシャッフルする
        locations.shuffle()

        // 3. シャッフルした座標にプレイヤーをテレポート
        alivePlayers.forEachIndexed { index, gamePlayer ->
            val player = gamePlayer.player!!
            val targetLocation = locations[index]

            player.teleport(targetLocation)

            // 演出用：テレポート音とエフェクト（お好みで調整してください）
            player.world.playSound(player.location, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
            player.world.spawnParticle(org.bukkit.Particle.PORTAL, player.location, 30, 0.5, 1.0, 0.5, 0.1)
        }

        Bukkit.broadcast(
            Component.text("🌀 空間が歪み、生存者の位置がシャッフルされた！", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
        )
    }
}