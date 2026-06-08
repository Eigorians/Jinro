package com.eastcompany.eastsub.jinro.manager

import com.eastcompany.eastsub.jinro.Jinro
import com.eastcompany.eastsub.jinro.item.shop.ItemRegistry
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.WanderingTrader
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.persistence.PersistentDataType

object ShopManager {

    private val plugin: Jinro get() = Jinro.instance
    private val SHOP_KEY = NamespacedKey(plugin, "is_shop")
    private val activeVillagers = mutableListOf<WanderingTrader>()

    fun setupShops() {
        clearActiveShops()
        spawnShops()
    }

    fun spawnShops() {
        val config = plugin.configManager.gameConfig
        val mapData = config.mapData[config.selectedMap] ?: return

        // 💡 Configのキー（"bread_1", "iron_sword" など）を元に、レジストリからCustomItemを取得して生成
        val shopItems = config.shopPrices.mapNotNull { (itemKey, price) ->
            // 個数付きのキー（"bread_5" など）に対応するため、必要ならレジストリ側かここで個数を分解する
            // 今回はシンプルに、キーが登録されていればそのCustomItemからItemStackを生成
            val customItem = ItemRegistry.get(itemKey) ?: return@mapNotNull null

            // 例: "bread_5" のようなキーなら数量を5、それ以外は1にする簡易判定
            val amount = if (itemKey.endsWith("_5")) 5 else 1

            val itemStack = customItem.create(amount)
            itemStack to price
        }

        mapData.shops.forEach {
            val trader = it.world?.spawnEntity(it, EntityType.WANDERING_TRADER) as? WanderingTrader ?: return@forEach

            trader.persistentDataContainer.set(SHOP_KEY, PersistentDataType.BYTE, 1.toByte())
            trader.setAI(false)
            trader.isInvulnerable = true
            trader.customName(Component.text("行商人", NamedTextColor.YELLOW))

            // ItemStackベースのレシピを設定
            trader.recipes = createRecipes(shopItems)

            trader.isSilent = true
            activeVillagers.add(trader)
        }
    }

    /**
     * 各CustomItemから作られたItemStackと、エメラルド価格を元に取引を設定
     */
    private fun createRecipes(items: List<Pair<ItemStack, Int>>): List<MerchantRecipe> {
        val recipes = mutableListOf<MerchantRecipe>()

        items.forEach { (resultItem, price) ->
            val recipe = MerchantRecipe(resultItem, 99999)

            // 基本コストとしてエメラルドを要求
            recipe.addIngredient(ItemStack(Material.EMERALD, price))

            // 💡 特殊ルール: 購入するものが「鉄の剣」なら、追加で「木の剣」を要求する
            if (resultItem.type == Material.IRON_SWORD) {
                // 要求する木の剣も、バニラではなくCustomItem(WoodSwordItem)から正しく生成したものにする
                ItemRegistry.get("wood_sword")?.let { woodSwordItem ->
                    recipe.addIngredient(woodSwordItem.create(1))
                } ?: run {
                    // 万が一レジストリにない場合のフォールバック
                    recipe.addIngredient(ItemStack(Material.WOODEN_SWORD, 1))
                }
            }

            recipes.add(recipe)
        }
        return recipes
    }

    fun clearActiveShops() {
        activeVillagers.forEach { it.remove() }
        activeVillagers.clear()
    }
}