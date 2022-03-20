/*
 * 🪟 ktor-sentry: Pluggable Ktor plugin to implement Sentry for error handling and request contexts.
 * Copyright (c) 2022 Noel <cutie@floofy.dev>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
