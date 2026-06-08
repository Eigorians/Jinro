package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.game.GameEndChecker
import com.eastcompany.eastsub.jinro.game.GamePlayer
import com.eastcompany.eastsub.jinro.game.JinroPhase
import com.eastcompany.eastsub.jinro.game.ResourceManager
import com.eastcompany.eastsub.jinro.game.Role
import com.eastcompany.eastsub.jinro.game.trap.TrapManager
import com.eastcompany.eastsub.jinro.item.JinroInventory
import com.eastcompany.eastsub.jinro.item.other.DummyItem
import com.eastcompany.eastsub.jinro.item.other.RoleBookManager
import com.eastcompany.eastsub.jinro.item.shop.ItemRegistry
import com.eastcompany.eastsub.jinro.listener.JinroPlayerListener
import com.eastcompany.eastsub.jinro.listener.ResourceListener // 💡 追加
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Bukkit.broadcastMessage
import org.bukkit.NamespacedKey
import org.bukkit.event.HandlerList
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.*

object JinroGameManager {
    private val plugin: Jinro get() = Jinro.instance

    private val role_view = Key.key("minecraft:role_view")

    private var gameListener: JinroPlayerListener? = null
    // ─── 💡 資源用リスナーのインスタンスを保持する変数を追加 ───
    private var resourceListener: ResourceListener? = null

    val gamePlayers = mutableMapOf<UUID, GamePlayer>()
    val activeSpectators = mutableSetOf<UUID>()
    var isGameRunning = false
    var trapManager: TrapManager? = null

    var resourceManager: ResourceManager? = null

    var currentPhase: JinroPhase = JinroPhase.FIRST_DAY
    var currentDayCount: Int = 1

    fun startGame(finalParticipants: Set<UUID>, finalSpectators: Set<UUID>) {
        isGameRunning = true
        currentPhase = JinroPhase.FIRST_DAY
        currentDayCount = 1
        gamePlayers.clear()
        activeSpectators.clear()

        if (gameListener == null) {
            gameListener = JinroPlayerListener()
            Bukkit.getPluginManager().registerEvents(gameListener!!, plugin)
        }

        // ─── 💡 ゲーム開始時に資源リスナーを動的に登録 ───
        if (resourceListener == null) {
            resourceListener = ResourceListener(plugin)
            Bukkit.getPluginManager().registerEvents(resourceListener!!, plugin)
        }

        finalParticipants.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                val gamePlayer = GamePlayer(offlinePlayer = player, role = Role.MURABITO)
                gamePlayer.resetState()
                gamePlayers[uuid] = gamePlayer
            }
        }

        finalSpectators.forEach { uuid ->
            val player = Bukkit.getPlayer(uuid)
            if (player != null && player.isOnline) {
                activeSpectators.add(uuid)
            }
        }

        // 役職配布の直後に安全チェックを実行
        JinroRoleManager.distributeRoles(gamePlayers)

        trapManager = TrapManager(plugin)
        resourceManager = ResourceManager(plugin)

        val endChecker = GameEndChecker(this)

        // 💡 修正: 新しい判定メソッドを呼び出す
        if (endChecker.isInitialStateInvalid()) {
            reset()
            broadcastMessage(
                Component.text("❌ 【ゲーム開始エラー】設定された役職のバランス、または参加人数が原因で、開始時点で終了条件を満たしているため強制終了しました。設定を見を見直してください。", NamedTextColor.RED, TextDecoration.BOLD)
            )
            return
        }

        JinroTeleportManager.teleportPlayersToGamePositions()

        JinroTeleportManager.teleportPlayersToGamePositions()
        ShopManager.setupShops()
        setInventory()
        respawnResource()

        sendRoleAssignmentTitles()

        object : BukkitRunnable() {
            override fun run() {
                if (!isGameRunning) return
                JinroTimeManager.startTimer()
            }
        }.runTaskLater(plugin, 100L)
    }

    fun respawnResource (){
        resourceManager?.spawnRandomResources(gamePlayers.size * 3)
    }

    private fun setInventory() {
        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach
            val personalRoleBook = RoleBookManager.createRoleBook(gPlayer.role)

            player.inventory.setItem(0, ItemRegistry.get("axe")?.create())
            player.inventory.setItem(1, ItemRegistry.get("pickaxe")?.create())
            // 7番と8番のスロットに固有アイテムをセット
            JinroInventory().createRoleBook(gPlayer,player,gPlayer.role)

            val dummyItem = DummyItem.create()
            for (slot in 9..35) {
                player.inventory.setItem(slot, dummyItem)
            }
        }
    }

    private fun sendRoleAssignmentTitles() {
        val times = Title.Times.times(
            Duration.ofMillis(500),
            Duration.ofMillis(4000),
            Duration.ofMillis(500)
        )

        gamePlayers.values.forEach { gPlayer ->
            val player = gPlayer.offlinePlayer.player ?: return@forEach
            val mainTitleComponent = Component.text("\uE000").font(role_view)
            val subTitleComponent = Component.text(gPlayer.role.role_view).font(role_view)
            val combinedTitle = Title.title(mainTitleComponent, subTitleComponent, times)
            player.showTitle(combinedTitle)

            val camp = gPlayer.role.camp
            val soundKey = net.kyori.adventure.key.Key.key(camp.soundName)
            val campSound = net.kyori.adventure.sound.Sound.sound(
                soundKey,
                net.kyori.adventure.sound.Sound.Source.MASTER,
                1.0f,
                1.0f
            )
            player.playSound(campSound)
        }
    }

    /**
     * ゲーム終了時、または強制停止時の完全初期化リセット
     */
    fun reset() {
        currentPhase = JinroPhase.FIRST_DAY
        currentDayCount = 1

        val graveKey = NamespacedKey.fromString("werewolf:grave")
        if (graveKey != null) {
            Bukkit.getWorlds().forEach { world ->
                world.entities.forEach { entity ->
                    if (entity is org.bukkit.entity.Interaction || entity is org.bukkit.entity.ItemDisplay) {
                        if (entity.persistentDataContainer.has(graveKey, PersistentDataType.STRING)) {
                            entity.remove()
                        }
                    }
                }
            }
        }
        trapManager?.clearAllTraps()
        trapManager = null

        resourceManager?.clearAllResources()
        resourceManager = null

        gamePlayers.clear()
        activeSpectators.clear()
        isGameRunning = false

        gameListener?.let { listener ->
            HandlerList.unregisterAll(listener)
            gameListener = null
        }

        // ─── 💡 ゲーム終了（リセット）時に資源用リスナーを確実に解除 ───
        resourceListener?.let { listener ->
            HandlerList.unregisterAll(listener)
            resourceListener = null
        }

        ShopManager.clearActiveShops()
        JinroTimeManager.stopTimer()
    }

    private fun broadcastMessage(component: Component) {
        for (player in Bukkit.getOnlinePlayers()) {
            player.sendMessage(component)
        }
    }
}