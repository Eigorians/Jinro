package com.eastcompany.eastsub.jinro.item.shop

import com.eastcompany.eastsub.jinro.game.item.CustomItem
import com.eastcompany.eastsub.jinro.item.shop.list.*

object ItemRegistry {
    /**
     * プラグイン内のすべてのカスタムアイテムを保持するマップ
     * 各オブジェクトの `id`（"normal_sword", "tnt_bomb" など）をキーとして自動的に紐付けます。
     */
    val ITEMS: Map<String, CustomItem> = listOf(
        // --- 武器・ツール系 ---
        NormalSwordItem,       // ボロい剣 (id: "normal_sword")
        IronSwordItem,
        AxeItem,               // 頑丈な斧 (id: "axe")
        PickaxeItem,           // 頑丈なツルハシ (id: "pickaxe")
        NormalBowItem,         // 頑丈な弓 (id: "normal_bow")
        NormalArrowItem,       // 普通の弓矢 (id: "normal_arrow")

        // --- 特殊能力ツール系 ---
        BlindnessToolItem,     // 盲目付与ツール (id: "blindness_tool")
        GlowingToolItem,       // 発光ツール (id: "glowing_tool")
        SameLookToolItem,      // 容姿統一ツール (id: "same_look_tool")

        // --- 消耗品・投擲・食料系 ---
        InvisibilityPotionItem,// 透明化のポーション (id: "invisibility_potion")
        SmokeBombItem,         // 煙玉 (id: "smoke_bomb")
        TntBombItem,           // 手投げ爆弾 (id: "tnt_bomb")
        BreadOneItem,          // 焼きたてのパン 1個 (id: "bread_1")
        BreadFiveItem          // 焼きたてのパン 5個 (id: "bread_5")
    ).associateBy { it.id }

    /**
     * ID文字列からカスタムアイテムの定義を取得します
     * @param id アイテムの識別子 (例: "tnt_bomb")
     * @return 該当するCustomItemオブジェクト。存在しない場合はnull
     */
    fun get(id: String): CustomItem? = ITEMS[id]
}