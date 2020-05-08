package com.manhhavu.jackson.xml

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Assert.assertThat
import org.junit.Test
import org.xmlunit.matchers.CompareMatcher.isIdenticalTo
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals

class JacksonXmlConverterTest {

    private val sampleXml = """<?xml version="1.0"?>
        <book id="bk101">
          <author>Gambardella, Matthew</author>
          <title>XML Developer's Guide</title>
          <genre>Computer</genre>
          <price currency="EUR">44.95</price>
          <publish_date>2000-10-01</publish_date>
          <description>An in-depth look at creating applications with XML.</description>
       </book>
    """.trimIndent()

    private val book = Book(
        id = "bk101",
        author = "Gambardella, Matthew",
        title = "XML Developer's Guide",
        genre = "Computer",
        price = Price(
            currency = "EUR",
            amount = BigDecimal("44.95")
        ),
        publishDate = "2000-10-01",
        description = "An in-depth look at creating applications with XML."
    )

    @Test
    fun testReceive() {
        val holder = AtomicReference<Book>()
        withTestApplication({
            install(ContentNegotiation) {
                register(ContentType.Application.Xml, JacksonXmlConverter())
            }
            routing {
                post("/") {
                    val book = call.receive<Book>()
                    holder.set(book)
                    call.respond("OK")
                }
            }
        }) {
            handleRequest(HttpMethod.Post, "/") {
                addHeader("Content-Type", "application/xml")
                setBody(sampleXml)
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val book = holder.get()
                assertEquals(holder.get(), book)
            }
        }
    }

    @Test
    fun testSend() {
        withTestApplication({
            install(ContentNegotiation) {
                register(ContentType.Application.Xml, JacksonXmlConverter())
            }
            routing {
                get("/") {
                    call.respond(book)
                }
            }
        }) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertThat(response.content, isIdenticalTo(sampleXml).ignoreWhitespace())
            }
        }
    }
}

@JacksonXmlRootElement(localName = "book")
data class Book(
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    val id: String,
    @JsonProperty("author")
    val author: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("genre")
    val genre: String,
    @JsonProperty("price")
    val price: Price,
    @JsonProperty("publish_date")
    val publishDate: String,
    @JsonProperty("description")
    val description: String
)

data class Price(
    @JacksonXmlProperty(localName = "currency", isAttribute = true)
    val currency: String,
    @JsonProperty(XML_TEXT_ELEMENT_NAME)
    @JacksonXmlText
    val amount: BigDecimal
)
