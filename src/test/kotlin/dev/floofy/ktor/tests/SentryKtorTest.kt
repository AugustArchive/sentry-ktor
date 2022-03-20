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
