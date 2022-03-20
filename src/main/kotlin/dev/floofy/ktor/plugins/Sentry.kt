package dev.floofy.ktor.plugins

import gay.floof.utils.slf4j.logging
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.sentry.*
import io.sentry.Sentry as SentryClient

/**
 * Represents the Sentry plugin that is used to configure using Sentry in this Ktor application.
 */
class Sentry(private val config: Configuration?) {
    private val breadcrumbThing = AttributeKey<Breadcrumb>("SentryHttpBreadcrumb")
    private val hub: IHub

    init {
        val currHub = SentryClient.getCurrentHub()
        hub = currHub.clone()
    }

    companion object: ApplicationFeature<ApplicationCallPipeline, ConfigurationBuilder, Sentry> {
        private val log by logging<Companion>()

        override val key: AttributeKey<Sentry> = AttributeKey("Sentry")
        override fun install(pipeline: ApplicationCallPipeline, configure: ConfigurationBuilder.() -> Unit): Sentry {
            log.debug("Initialising Sentry plugin...")

            val config = ConfigurationBuilder().apply(configure).build()
            if (config != null) {
                log.debug("Constructing Sentry client...")
                SentryClient.init {
                    it.dsn = config.dsn
                    config.configure?.invoke(it)
                }

                log.debug("Done!")
            }

            val sentry = Sentry(config)
            sentry.install(pipeline)

            return sentry
        }
    }

    private fun install(context: ApplicationCallPipeline) {
        context.intercept(ApplicationCallPipeline.Call) {
            val url = "${call.request.httpVersion} ${call.request.httpMethod.value} ${call.request.path()}"
            val transaction = SentryClient.startTransaction("Ktor Request", url)
            val breadcrumb = call.attributes.getOrNull(breadcrumbThing)

            try {
                proceed()

                transaction.status = SpanStatus.OK
            } catch (e: Exception) {
                transaction.apply {
                    this.throwable = e
                    this.status = SpanStatus.INTERNAL_ERROR
                }

                breadcrumb?.level = SentryLevel.ERROR
                SentryClient.captureException(e)
                throw e
            } finally {
                if (breadcrumb != null) {
                    hub.addBreadcrumb(breadcrumb)
                    call.attributes.remove(breadcrumbThing)
                }

                transaction.finish()
            }
        }

        context.intercept(ApplicationCallPipeline.Setup) {
            val url = "${call.request.httpVersion} ${call.request.httpMethod.value} ${call.request.path()}"
            val breadcrumb = Breadcrumb.http(url, call.request.httpMethod.value)

            call.attributes.put(breadcrumbThing, breadcrumb)
        }

        context.sendPipeline.intercept(ApplicationSendPipeline.Transform) {
            val breadcrumb = Breadcrumb.info("Send Pipeline: Transform")
            hub.addBreadcrumb(breadcrumb)
        }

        context.sendPipeline.intercept(ApplicationSendPipeline.ContentEncoding) {
            val breadcrumb = Breadcrumb.info("Send Pipeline: Content Encoding")
            hub.addBreadcrumb(breadcrumb)
        }

        context.sendPipeline.intercept(ApplicationSendPipeline.After) {
            val breadcrumb = Breadcrumb.info("After Send Pipeline")
            hub.addBreadcrumb(breadcrumb)
        }
    }
}
