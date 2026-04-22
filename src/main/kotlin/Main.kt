package net.vanolex

import com.typesafe.config.ConfigFactory
import io.github.config4k.extract
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.cio.*
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

fun main() {

    config = ConfigFactory.load().extract()

    mysqlConnection = DriverManager.getConnection("jdbc:mariadb://${config.dbHost}:3306/${config.dbDatabase}", config.dbUsername, config.dbPassword)

    server = embeddedServer(
        CIO,
        configure = {
            connectionIdleTimeoutSeconds = 45

        },
        port = config.ktorPort,
        host = config.ktorHost,
        module = Application::module
    )

    server.start(true)
}

fun Application.module() {
    routing {
        get(Regex(".*")) {
            val path = call.request.path().removeSuffix("/").removePrefix("/") // 127.0.0.1/hello/ -> path == "hello"

            when (path) {

                "" -> {
                    call.respondBytes(
                        contentType = ContentType.Text.Html,
                        bytes = File("assets/index.html").readBytes()
                    )
                }

                "styles.css" -> {
                    call.respondBytes(
                        contentType = ContentType.Text.CSS,
                        bytes = File("assets/styles.css").readBytes()
                    )
                }

                "script.js" -> {
                    call.respondBytes(
                        contentType = ContentType.Text.JavaScript,
                        bytes = File("assets/script.js").readBytes()
                    )
                }

                "favicon.ico" -> {
                    call.respondBytes(
                        contentType = ContentType.Image.XIcon,
                        bytes = File("assets/favicon.ico").readBytes()
                    )
                }

                "check.svg" -> {
                    call.respondBytes(
                        contentType = ContentType.Image.SVG,
                        bytes = File("assets/check.svg").readBytes()
                    )
                }

                "copy.svg" -> {
                    call.respondBytes(
                        contentType = ContentType.Image.SVG,
                        bytes = File("assets/copy.svg").readBytes()
                    )
                }

                else -> {
                    if (path.startsWith("qr/")) {
                        processCode(call, path.removePrefix("qr/").removeSuffix(".png"))
                        return@get
                    }

                    if (!isValidCode(path)) {
                        call.respondBytes(
                            contentType = ContentType.Text.Html,
                            bytes = File("assets/404.html").readBytes()
                        )
                        return@get
                    }

                    val link = getLink(path)

                    if (link == null) {
                        call.respondBytes(
                            contentType = ContentType.Text.Html,
                            bytes = File("assets/404.html").readBytes()
                        )
                        return@get
                    }

                    call.respondRedirect(link)
                    }
            }
        }

        post("/api/genurl") {
            val clientIP = call.request.headers["CF-Connecting-IP"] ?: "default"

            if (!isRequestAllowed(clientIP)) {
                call.response.status(HttpStatusCode.TooManyRequests)
                call.respondText("Sending requests too quickly. Please wait a few seconds before trying again.")
                return@post
            }

            val link = try {
                call.receiveText()
            } catch(e: BadRequestException) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("Request missing `Content-Type` header.")
                return@post
            }

            if (link.length > 512) {
                call.response.status(HttpStatusCode.PayloadTooLarge)
                call.respondText("The URL is too long! Maximum allowed length is 512 characters.")
                return@post
            }

            if (!isValidUrl(link)) {
                call.response.status(HttpStatusCode.BadRequest)
                call.respondText("Invalid URL! Please enter a valid web address.")
                return@post
            }

            updateBlock(clientIP, 10)

            val code = try {
                addLink(link, clientIP)
            } catch (e: SQLException) {
                call.application.environment.log.error("SQLException: ${e.errorCode} - ${e.sqlState}")
                call.response.status(HttpStatusCode.Forbidden)
                call.respondText("Internal database error! Please contact the website administrator.")
                return@post
            }

            call.response.status(HttpStatusCode.OK)
            call.respondText(code)

        }
    }
}
