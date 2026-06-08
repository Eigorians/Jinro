package com.eastcompany.eastsub.jinro.item.job.list

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.TargetFinder
import com.eastcompany.eastsub.jinro.manager.JinroGameManager
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class Shaman_Item : SpecialItem() {
    override val tag = "shaman_0"
    override val lore = listOf("お墓を\uE501で霊能し、役職を導き出す")
    override val material = Material.STICK // 必要に応じて変更してください

    override val role = Role.SHAMAN // 💡 霊能者のRole（enum名に合わせて調整してください）
    override val displayName = role.roleName + "スキル 霊能する 墓を\uE501"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand

        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0
            if (isCooltime == 1.toByte()) {
                return
            }
        }

        // 4マス以内のターゲットを検索
        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()

        // ターゲットがプレイヤーではなく、エンティティ（お墓のInteraction）であることをチェック
        if (target !is ClickTarget.EntityTarget) return
        val hitEntity = target.entity as? Interaction ?: return

        // 永続化キーを定義してお墓のデータをロード
        val graveKey = NamespacedKey.fromString("werewolf:grave") ?: return
        val ownerKey = NamespacedKey.fromString("werewolf:owner") ?: return

        // これが有効なお墓（Interaction）かチェック
        if (!hitEntity.persistentDataContainer.has(graveKey, PersistentDataType.STRING)) return

        // お墓に記録されている死者のUUID文字列を取得
        val deadPlayerUuidStr = hitEntity.persistentDataContainer.get(ownerKey, PersistentDataType.STRING) ?: return
        val deadPlayerUuid = UUID.fromString(deadPlayerUuidStr)

        // 死亡したプレイヤーのゲームデータを取得
        val deadGamePlayer = JinroGameManager.gamePlayers[deadPlayerUuid] ?: return

        // クールダウンアイテムに入れ替える（共通化メソッド）
        val cooltimeItem = this.createCooldownItem()
        player.inventory.setItemInMainHand(cooltimeItem)

        // ❌ 偽物対策: 使用者の役職が「霊能者」ではない場合、でたらめな結果
        if (gamePlayer.role != this.role) {
            val randomRole = Role.entries.filter { it != this.role }.random()

            player.sendMessage("§d[霊能結果] ${deadGamePlayer.name} は " + randomRole.roleName + " §dのようだ。")
            return
        }

        player.sendMessage("§d[霊能結果] ${deadGamePlayer.name} は " + deadGamePlayer.role.roleName + " §dのようだ。")
    }
}