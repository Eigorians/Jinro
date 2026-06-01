package com.eastcompany.eastsub.jinro.game

enum class AbilityStatus {
    AVAILABLE,    // 使用可能
    UNAVAILABLE,  // 使用不可（夜じゃないから使えない、など一時的な制限）
    FORBIDDEN     // 絶対不可（使い切った、能力を奪われた、など永続的な制限）
}