package dev.floofy.ktor.tests

import dev.floofy.ktor.plugins.Sentry
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import io.sentry.SentryOptions

class SentryKtorTest: DescribeSpec({
    describe("dev.floofy.ktor.plugins.Sentry") {
        it("should sent an error") {
            withTestApplication({
                this.install(Sentry) {
                    // where is the part i care that this is leaked
                    // you can't gain access to this anyway :)
                    dsn = "https://2049c644a89942cf89ca65d4641f0970@sentry.floof.gay/8"

                    configure {
                        it.tracesSampleRate = 1.0
                        it.tracesSampler = SentryOptions.TracesSamplerCallback {
                            1.0
                        }
                    }
                }

                this.install(StatusPages) {
                    exception<Exception> { cause ->
                        call.respond(HttpStatusCode.InternalServerError, "{\"success\":false,\"message\":\"${cause.message}\"}")
                    }
                }

                this.routing {
                    route("/", HttpMethod.Get) {
                        handle {
                            throw Exception("get fucked!")
                        }
                    }
                }
            }) {
                handleRequest(HttpMethod.Get, "/").apply {
                    response.status() shouldNotBe null
                    response.status() shouldBe HttpStatusCode.InternalServerError
                    response.content shouldNotBe null
                    response.content shouldBe "{\"success\":false,\"message\":\"get fucked!\"}"
                }
            }
        }
    }
})
