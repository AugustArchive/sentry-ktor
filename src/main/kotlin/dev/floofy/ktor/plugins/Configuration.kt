package dev.floofy.ktor.plugins

import gay.floof.utils.slf4j.logging
import io.sentry.SentryOptions

/**
 * Represents a minimal configuration for the [Sentry] plugin.
 */
data class Configuration(
    /**
     * The DSL object to build a new Sentry client when called from [io.sentry.Sentry.init].
     */
    val configure: ((SentryOptions) -> Unit)? = null,

    /**
     * The DSN to use when connecting to Sentry.
     */
    val dsn: String
)

/**
 * Builder for configuring the Sentry plugin.
 */
class ConfigurationBuilder {
    private val log by logging<ConfigurationBuilder>()

    /**
     * The DSN to use when connecting to Sentry.
     */
    var dsn: String = ""

    /**
     * The DSL object to build a new Sentry client when called from [io.sentry.Sentry.init].
     */
    private var configureBuilder: ((SentryOptions) -> Unit)? = null

    /**
     * If we should use the pre-existing Sentry instance that was already constructed,
     * if [io.sentry.Sentry.isEnabled] is set to `true`.
     */
    var useExisting: Boolean = true

    fun configure(configure: ((SentryOptions) -> Unit) = {}): ConfigurationBuilder {
        configureBuilder = configure
        return this
    }

    /**
     * Builds this [Configuration] object. Returns `null` if we should use
     * a pre-existing Sentry instance.
     */
    fun build(): Configuration? {
        if (dsn.isEmpty()) {
            log.debug("Checking if we should use pre-existing Sentry instance...")

            if (useExisting && io.sentry.Sentry.isEnabled()) {
                log.debug("Using pre-existing Sentry instance since a client was already established and ConfigurationBuilder.useExisting is set to true.")
                return null
            }
        } else {
            if (useExisting && io.sentry.Sentry.isEnabled()) {
                log.debug("Pre-existing Sentry client already is initialized, skipping.")
                return null
            }
        }

        return Configuration(configureBuilder, dsn)
    }
}
