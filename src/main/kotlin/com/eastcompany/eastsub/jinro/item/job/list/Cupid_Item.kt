package com.eastcompany.eastsub.jinro.item

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.Camp
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.item.job.SpecialItem
import com.eastcompany.eastsub.jinro.util.ClickTarget
import com.eastcompany.eastsub.jinro.util.TargetFinder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType

class Cupid_Item : SpecialItem() {
    override val tag = "cupid_0"
    override val lore = listOf("2人を恋人陣営にする（2人選択時に発動）")
    override val material = Material.STICK

    override val role = Role.CUPID
    override val displayName = role.roleName + "スキル 恋の矢"
    override val model_key = tag
    override val cooldown_model_key = model_key + "_cooltime"
    private val plugin = Jinro.instance

    override fun onRightClick(
        player: Player,
        gamePlayer: GamePlayer,
        event: PlayerInteractEvent
    ) {
        // 1. クールダウンチェック
        val cooltimeKey = NamespacedKey(plugin, cooldown_model_key)
        val currentItem = player.inventory.itemInMainHand
        if (currentItem.hasItemMeta()) {
            val isCooltime = currentItem.itemMeta.persistentDataContainer.get(cooltimeKey, PersistentDataType.BYTE) ?: 0
            if (isCooltime == 1.toByte()) return
        }

        // 2. ターゲットの取得
        val targetFinder = TargetFinder(player, 4.0)
        val target = targetFinder.findTarget()
        if (target !is ClickTarget.GamePlayerTarget) return

        val targetGamePlayer = target.gamePlayer
        val targetPlayer = targetGamePlayer.player ?: return

        // 3. 自分自身をターゲットにしている場合はキャンセル
        if (targetGamePlayer == gamePlayer) {
            player.sendMessage(Component.text("自分自身を恋人に選ぶことはできません。").color(NamedTextColor.RED))
            return
        }

        // === 選択人数のカウントと分岐処理 ===
        val firstTarget = gamePlayer.firstLoveTarget

        if (firstTarget == null) {
            // 【1人目の選択】（偽物でも通常通り進む）
            // 💡 1人目として選ぶ対象がそもそも死亡していないかチェック（念のため）
            if (!targetGamePlayer.isAlive) {
                player.sendMessage(Component.text("死亡しているプレイヤーを恋人に選ぶことはできません。").color(NamedTextColor.RED))
                return
            }

            gamePlayer.firstLoveTarget = targetGamePlayer

            player.sendMessage(
                Component.text("1人目の恋人として ")
                    .append(Component.text(targetPlayer.name).color(NamedTextColor.GREEN))
                    .append(Component.text(" を選びました。もう1人選択してください。"))
            )
        } else {
            // 【2人目の選択】

            // 💡 ✨【追加された訂正条件】1人目に選んでいたプレイヤーが既に死亡している場合
            if (!firstTarget.isAlive) {
                player.sendMessage(
                    Component.text("1人目に選択したプレイヤーが死亡したため、選択がリセットされました。最初から選び直してください。")
                        .color(NamedTextColor.RED)
                )
                gamePlayer.firstLoveTarget = null // データをリセットして仕切り直し
                return
            }

            if (firstTarget == targetGamePlayer) {
                player.sendMessage(Component.text("1人目とは異なるプレイヤーを選んでください。").color(NamedTextColor.RED))
                return
            }

            // 💡 2人目として選ぶ対象が死亡していないかチェック
            if (!targetGamePlayer.isAlive) {
                player.sendMessage(Component.text("死亡しているプレイヤーを恋人に選ぶことはできません。").color(NamedTextColor.RED))
                return
            }

            val firstPlayer = firstTarget.player ?: return

            // 2回クリック完了のため、アイテムをクールダウン（使用不可）に変更（偽物でも消費される）
            val cooltimeItem = this.createCooldownItem()
            player.inventory.setItemInMainHand(cooltimeItem)

            // キューピット（本人）へのメッセージ通知（偽物でも「引き込んだ」と表示される）
            player.sendMessage(
                Component.text("恋人の矢を放ち、")
                    .append(Component.text(firstPlayer.name).color(NamedTextColor.GREEN))
                    .append(Component.text(" と "))
                    .append(Component.text(targetPlayer.name).color(NamedTextColor.GREEN))
                    .append(Component.text(" を恋人陣営に引き込みました。"))
            )

            // 次のターンのために選択データをリセット
            gamePlayer.firstLoveTarget = null

            // ⚠️★ここで役職不一致チェック（ポンコツ効果：メッセージだけ見せて内部処理を完全不発にする）
            if (gamePlayer.role != this.role) {
                return
            }

            // === 本物のキューピットだけが通過できる内部データ処理 ===
            firstTarget.isLovers = true
            firstTarget.camp = Camp.LOVERS

            targetGamePlayer.isLovers = true
            targetGamePlayer.camp = Camp.LOVERS

            // 恋人たちに送る【本物の通知】（本物の時だけ2人に送られる）
            val notificationComponent = Component.text()
                .append(Component.text("【運命の繋がり】\n").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("あなたはキューピットの矢によって").color(NamedTextColor.WHITE))
                .append(Component.text("恋人陣営").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD))
                .append(Component.text("になりました。\n\n").color(NamedTextColor.WHITE))
                .append(Component.text("■ 勝利条件\n").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text("恋人2人が生存した状態でゲームが終了する").color(NamedTextColor.YELLOW))
                .build()

            firstPlayer.sendMessage(notificationComponent)
            targetPlayer.sendMessage(notificationComponent)
        }
    }
}