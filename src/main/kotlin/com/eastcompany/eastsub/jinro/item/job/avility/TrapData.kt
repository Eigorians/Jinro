package com.eastcompany.eastsub.jinro.item.job.avility

import org.bukkit.Location
import java.util.*

data class TrapData(
    val id: UUID = UUID.randomUUID(),
    val location: Location,
    val ownerUuid: UUID,
    val isDummy: Boolean,
    var isInactive: Boolean = true
)