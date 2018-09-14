package com.cordagen.web.controllers

import com.cordagen.web.NodeRPCConnection
import net.corda.core.contracts.ContractState
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * A CorDapp-agnostic controller that exposes standard endpoints.
 */
@RestController
@RequestMapping("/") // The paths for GET and POST requests are relative to this base path.
class StandardController(
        private val rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    val proxy = rpc.proxy

    @GetMapping(value = "/status", produces = [MediaType.TEXT_PLAIN_VALUE])
    private fun status() = "200"

    @GetMapping(value = "/servertime", produces = [MediaType.TEXT_PLAIN_VALUE])
    private fun serverTime() = LocalDateTime.ofInstant(proxy.currentNodeTime(), ZoneId.of("UTC")).toString()

    @GetMapping(value = "/addresses", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun addresses() = proxy.nodeInfo().addresses.toString()

    @GetMapping(value = "/identities", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @GetMapping(value = "/platformversion", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun platformVersion() = proxy.nodeInfo().platformVersion.toString()

    @GetMapping(value = "/peers", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @GetMapping(value = "/notaries", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun notaries() = proxy.notaryIdentities().toString()

    @GetMapping(value = "/flows", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun flows() = proxy.registeredFlows().toString()

    @GetMapping(value = "/states", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    private fun states() = proxy.vaultQueryBy<ContractState>().states.toString()

    fun progressError(m: String?, t: Throwable) = logger.error(m, t)
    fun progressInfo (m: String) = logger.info(m)

    fun throwableAsJson(m: String?, t: Throwable): Map<String, Any> {
        progressError(m, t)
        return mapOf("errors" to listOf(t.message ?: t.javaClass.simpleName))
    }
}