package com.cordagen.commons

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.transactions.SignedTransaction

/**
 * Abstract flow definitions allow them to be used in specific modules as different implementations and
 * at the same time implement responder flows against these common class names, not particular
 * implementations.
 */
@InitiatingFlow
abstract class SampleInitiatingFlow() : FlowLogic<SignedTransaction>()

@InitiatingFlow
abstract class GenericFlowDefinition() : FlowLogic<SignedTransaction>()