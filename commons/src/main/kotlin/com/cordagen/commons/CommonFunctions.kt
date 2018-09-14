package com.cordagen.commons

import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub

fun ServiceHub.notary(): Party = this.networkMapCache.notaryIdentities.firstOrNull()
        ?: throw IllegalStateException("No available notary.")

